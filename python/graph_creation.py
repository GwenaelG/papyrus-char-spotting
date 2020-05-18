# -*- coding: utf-8 -*-
"""
Created on Sat May  9 10:58:17 2020

@author: Gwenael
"""

import numpy as np
import cv2 as cv
import matplotlib.pyplot as plt
import os
import glob
import skimage.morphology 
import itertools
import copy

# =============================================================================
# Keypoint pseudocode
#  
# input img, d
# return graph(V,E)
# 
# find CC
# for each CC:
#     add endpoints and junction points to V 
#     remove junction points from CC
#     for each CSC:
#         add d-equid points to V
# for each pair u,v in V:
#     add u,v to E if u,v are_neighbours
#     
# return V,E
# 
# =============================================================================


def skeletonize(img):
    """
    Skeletonizes an image, using scikit's function

    Parameters
    ----------
    img : int array
        preprocessed binary image (0 foreground, 255 background)
            
    Returns
    -------
    skel_coord : array
        coordinates of the skeleton nodes 

    """
    t, img = cv.threshold(img, 200, 1, cv.THRESH_BINARY_INV)
    skel_img = skimage.morphology.skeletonize(img)
    # coordinates of the skeleton pixels
    # transposition for clearer coordinates (shape is (n,2))
    skel_coord = np.array(np.where(skel_img)).T
    return skel_coord


def find_CC(neigh_dict):
    """
    Find the connected components in a skeletonized image
    code adapted from https://stackoverflow.com/a/50639220
    
    Parameters
    ----------
    neigh_dict : dictionary
        dictionary of neighbours

    Returns
    -------
    list_CC : list of lists 
        list of the CCs as list of indices
    
    """
    visited = set()
    list_CC = []
    for i in neigh_dict.keys():
        if i not in visited:
            cc, visited = get_cc(i ,visited, neigh_dict)
            list_CC.append(cc)       
    return list_CC

def fill_neighbours(coords):
    """
    fills a dictionary with neighbour relationships from coordinates

    Parameters
    ----------
    coords : list
        list of coordinates

    Returns
    -------
    neigh_dict : dictionary
        dictionary of neighbours

    """
    N = len(coords)
    # shorter creation possible with comprehension?
    neigh_dict = {}
    for i in range(N):
        neigh_dict[i] = set()
    #all pairs without repetition (ordered pairs)
    for (i,j) in itertools.permutations(range(N),2):
        if are_neighbours(coords[i], coords[j]):
            nb_i = neigh_dict[i]
            nb_i.add(j)
            neigh_dict[i] = nb_i
    return neigh_dict 


def are_neighbours(p1, p2):
    """
    

    Parameters
    ----------
    p1 : list
        Coordinates of the first node
    p2 : list
        coordinates of the second node

    Returns
    -------
    boolean
        whether the two points are 8-neighbours

    """
    return (abs(p1[0]-p2[0]) <= 1 and abs(p1[1]-p2[1]) <= 1)


def get_cc(i, visited, neigh_dict):
    """
    get the connected component of a given node
    code adapted from https://stackoverflow.com/a/50639220

    Parameters
    ----------
    i : int
        node index 
    visited : set
        lists the visited nodes
    neigh_dict : dictionary
        key: nodes, entry is its neighbors

    Returns
    -------
    cc : list
        list of nodes of the connected component
    visited : set
        updated visited nodes

    """
    cc = []
    # next to be visited
    queue = set([i])
    while queue:
        j = queue.pop()
        visited.add(j)
        # add unvisited neighbors to queue
        queue.update(neigh_dict[j] - visited)
        cc.append(j)
    return cc, visited    


def update_neighbours(CC, neigh_list):
    """
    creates a neighbour dictionary for a CC

    Parameters
    ----------
    CC : list
        list of indices of the connected component
    neigh_list : dictionary
        neighbours of the whole graph

    Returns
    -------
    neigh_CC : dictionary
        neighbours for the nodes of the connected component

    """
    neigh_CC = {}
    for i in CC:
        nb = set()
        nb.update(neigh_list[i])
        neigh_CC[i] = nb        
    return neigh_CC

def find_keypoints(neigh_CC):
    """
    finds endpoints and junction points in a connected component
    conditions
    - keypoints have only one neighbour
    - junction points have three or more neighbours
    - circular structures appear when no endpoints are found 
        -> take first node as arbitrary junction point
    TODO: evtl. recognize endpoint with two neighbours
    TODO: evtl. dont allow neighbouring endpoint and junction point

    Parameters
    ----------
    neigh_CC : dictionary
        neighbours in the CC

    Returns
    -------
    endpoints : list
        indices of the endpoints
    junctionpoints : list
        indices of the junction points

    """
    endpoints = []
    junctionpoints = []
    degrees = {}
    # count neighbours
    for node in neigh_CC:
        degrees[node] = len(neigh_CC[node])
    for node in neigh_CC:
        neighbours = neigh_CC[node]
        # check for endpoint
        if degrees[node] in [0,1]:
            solo = True
            # if neighbour already endpoint (i.e. two-pixel segment), dont add
            for nb in neighbours:
                    if nb in endpoints:
                        solo = False 
            if solo:
                endpoints.append(node)
        # check for junction point
        if degrees[node] > 2:
            best = True
            for nb in neighbours:
                #look for neighbouring junction point
                if nb in junctionpoints:
                    # if neighbour has higher degree, no new junction point
                    if degrees[nb] >= degrees[node]:
                        best = False
                    # if lower degree, remove neighbour     
                    else:
                        junctionpoints.remove(nb)
            if best:
                junctionpoints.append(node)
    # check for circular structure
    # condition: no endpoint (?)
    if len(endpoints) == 0:
        #arbitrary junction point at first node
        junctionpoints.append(list(neigh_CC.keys())[0])
    return endpoints, junctionpoints


def add_equidpoints(neigh_CSC, D):
    """
    adds new points every D pixels, begins at one end of the CSC
    CSC is a segment (length may be 1)
    
    TODO: evtl slide first point such that both ends have approx the same number
        of empty pixels 
    TODO: evtl change CSC generation / junction point recognition to stop CSC
        from being circular and thus having no endpoint 
        problem occurs in such structures:
        x x x
        x   x x
        x x x 

    Parameters
    ----------
    neigh_CSC : dictionary 
        neighbours in the CSC
    D : int
        distance between equidistant pixels

    Returns
    -------
    equidpoints : list
        list on indices of intermediate points

    """
    equidpoints = []
    node = None
    for i in neigh_CSC:
        # if CSC only has one pixel
        if len(neigh_CSC[i]) == 0:
            return []
        # if we found an end of segment
        if len(neigh_CSC[i]) == 1:
            node = i
            break
    # still possible that a CSC has no endpoint
    if node is None:
        node = list(neigh_CSC.keys())[0]
    count = 0
    visited = set()
    while True:
        count += 1
        visited.add(node)
        if count % D == 0:
        # if count 
            equidpoints.append(node)
        for n in neigh_CSC[node]:
            if n not in visited:
                next_node = n
        node = next_node
        if count == len(neigh_CSC.keys()):
            break
    return equidpoints
   
    
# =============================================================================
# def fill_edges_v1(V, neigh_img):
#     """
#     older algo
#     problem when two junction points are neighbours:
#     the second one does not disappear
#     
#     
#     creates edges between between directly connected keypoints
# 
#     Parameters
#     ----------
#     V : list
#         indices of the keypoints
#     neigh_img : dict
#         neighbours of the whole skeletonized image
# 
#     Returns
#     -------
#     E : set
#         set with tuples representing the edges of the graph
# 
#     """
#     E = set()
#     for node in V:
#         visited = set([node])
#         queue = set(neigh_img[node])
#         while queue:
#             next_node = queue.pop()
#             visited.add(next_node)
#             # we found another vertex of V
#             if next_node in V:
#                 # tuples are sorted s. th. edges are not added twice in E
#                 E.add(tuple(sorted((node, next_node))))
#             else:
#                 #add unvisited neighbour nodes to queue
#                 for i in neigh_img[next_node]:
#                     if i not in visited:
#                         queue.add(i)     
#     return E
# =============================================================================
    

def fill_edges(V, neigh):
    """
    adds edge for adjacent keypoints in the skeleton
    the algorithm "colors" the skeleton starting from each keypoint(/color):
    it colors the unvisited neighbours of one color, then those of the next
    color and so on until all nodes are visited ,and then one last run for all
    colors. Trying to color a node of another color means the respective
    keypoints are adjacent    
        
    Parameters
    ----------
    V : list
        indices of the keypoints
    neigh : dict
        neighbours of the whole skeleton

    Returns
    -------
    E : set
        set with tuples representing the edges of the graph

    """
    E = set()
    visited = {}
    # stores the frontmost pixels of each keypoint spreading
    current = {}
    for n in V:
        # each keypoint is a start point for coloring
        current[n] = set({n})
        # keep track of all visited nodes with origin keypoint 
        visited[n] = n
    full = False
    while len(visited.keys()) <= len(neigh.keys()):
        # spread each keypoint once
        for orig in V:
            # get front most nodes
            nodes = current[orig]
            temp = set()
            for node in nodes:
                for nb in neigh[node]:
                    # if new node, add to next frontmost pixels
                    if nb not in visited:
                        temp.add(nb)
                        visited[nb] = orig
                    # if visited node of other origin, add edge in E
                    elif visited[nb] != orig:
                        # sort tuple to avoid duplicate edges
                        E.add((tuple(sorted((visited[nb], orig)))))                                    
                current[orig] = temp
        # algo needs one more run afterwards
        # to allow for last connections to be made
        if full:
            break
        full = len(visited.keys()) == len(neigh.keys())            
    return E


def display_img_graph(img, V, E, coord, name):
    """
    display the skeleton with red endpoints, blue junction points and gray 
    equidistant points in a first subplot, and the final graph on top of the 
    word image in the second subplot

    Parameters
    ----------
    img : array
        preprocessed image
    V : list
        V[0] contains indices of all keypoints
        V[1] contains indices of endpoints
        V[2] contains indices of junction points
        V[3] contains indices of equidistant points
    E : set
        edges stored as tuples
    coord : list
        list of coordinates of the skeleton's nodes
    
    Returns
    -------
    None.

    """
    # one subplot with skeleton and keypoints
    img_skel = np.full((img.shape[0], img.shape[1],3), 0)
    for node in coord:
        img_skel[node[0],node[1],:] = (255,255,255)
    for ep in V[1]:
        pos = coord[ep]
        img_skel[pos[0],pos[1],:] = (255,0,0)
    for jp in V[2]:
        pos = coord[jp]
        img_skel[pos[0],pos[1],:] = (0,0,255)
    for eqdp in V[3]:
        pos = coord[eqdp]
        img_skel[pos[0],pos[1],:] = (150,150,150)
    # plt.subplot(2,1,1)
    # plt.imshow(img_skel)
    # one subplot with word image and graph
    img_rgba = cv.cvtColor(img, cv.COLOR_GRAY2RGBA)
    alpha = np.full((img.shape[0], img.shape[1]), 50)
    img_rgba[:,:,3] = alpha
    for edge in E:
        # inverted x and y!
        # 1st index (rows) corresponds to y-coord
        # 2nd index (columns) corresponds to x-coord
        n1 = edge[0]
        x1 = coord[n1][1]
        y1 = coord[n1][0]
        n2 = edge[1]
        x2 = coord[n2][1]
        y2 = coord[n2][0]
        cv.line(img_rgba, (x1, y1), (x2, y2), (0, 0, 0, 100), 1)   
    for node in V[0]:
        pos = coord[node]
        img_rgba[pos[0],pos[1]] = (0,0,0,255)
    # plt.subplot(2,1,2)
    # plt.imshow(img_rgba)
    location = 'C:/Users/Gwenael/Desktop/MT/graphs-gwenael/GW/keypoint/'
    plt.imsave(location+name+'.jpg', img_rgba)
    # plt.tight_layout()
    # plt.show()

    
def keypoint(img, D, name):
    """
    main function, calls all the others

    Parameters
    ----------
    img : uint8 array (OpenCV image)
        The preprocessed word image 
    D : int
        Distance between two equidistant points.
        See Graph-Based KWS book, Table 9.1 to 9.4 for parameters for the
        different graph sizes and the different databases
    name : string
        name of the file    

    Returns
    -------
    V : TYPE
        DESCRIPTION.
    E : TYPE
        DESCRIPTION.

    """
    # transform image into skeleton
    skel_coord = skeletonize(img)
    # fill neighbours dictionary
    neigh_img = fill_neighbours(skel_coord)
    # separate into connected components
    list_CC = find_CC(neigh_img)
    # vertices and edges of final graph
    V = [[],[],[],[]]
    for CC in list_CC:
        # dictionary of neighbours in CC
        neigh_CC = update_neighbours(CC, neigh_img)
        # find endpoints and junction points
        endpoints, junctionpoints = find_keypoints(neigh_CC)
        for ep in endpoints:
            V[0].append(ep)
            V[1].append(ep)
        for jp in junctionpoints:
            V[0].append(jp)
            V[2].append(jp)
        # update neighbours dict by removing junction points
        updated_neigh = copy.deepcopy(neigh_CC)
        for i in neigh_CC:
            # remove junction points from dictionary
            if i in junctionpoints:
                updated_neigh.pop(i)
            else:
                nb = updated_neigh[i]
                for j in neigh_CC[i]:
                    # remove junction points' entries in their neighbours
                    if j in junctionpoints:
                        nb.remove(j)
                updated_neigh[i] = nb
        # find connected sub components
        list_CSC = find_CC(updated_neigh)
        for CSC in list_CSC:
            # dictionary of neighbours in CSC
            neigh_CSC = update_neighbours(CSC, updated_neigh)
            # find equidistant points
            equid_points = add_equidpoints(neigh_CSC, D)
            # dont add a point if it already has a neighbour in V
            for node in equid_points:
                isolated = True
                for nb in V[0]:
                    if are_neighbours(skel_coord[node], skel_coord[nb]):
                        isolated = False
                        break
                if isolated:
                    V[0].append(node)
                    V[3].append(node)
    # fill edges list
    E = fill_edges(V[0], neigh_img)
    # show img
    display_img_graph(img, V, E, skel_coord, name)
    

location = 'C:/Users/Gwenael/Desktop/MT/histograph-master/01_GW/00_WordImages/'
fileset = glob.glob(location+'*.png')
for n, f in enumerate(fileset):
    img1 = cv.imread(f, 0)
    name = f[-13:-4]
    keypoint(img1, 8, name)

# n = 5    
# img = cv.imread(fileset[n], 0)
# name = fileset[n][-13:-4]
keypoint(img, 8, name)
    
    