# -*- coding: utf-8 -*-
"""
Created on Sat May  9 10:58:17 2020

@author: Gwenael
"""

import numpy as np
import cv2 as cv
import matplotlib.pyplot as plt
import os
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
    # transposition for clearer coordinates (shape is (n,2)
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
        TODO: recognize endpoint with two neighbours
    - junction points have three or more neighbours
    - circular structures appear when no endpoints are found 
        -> take first node as arbitrary junction point

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


def find_equidpoints(neigh_CSC, D):
    #merge with junction point if in 8-nbhd
    equidpoints = []
    return equidpoints
    

def keypoint(img, D):
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
    V = []
    E = []
    for CC in list_CC:
        # dictionary of neighbours in CC
        neigh_CC = update_neighbours(CC, neigh_img)
        # find endpoints and junction points
        endpoints, junctionpoints = find_keypoints(neigh_CC)
        V.append(endpoints)
        V.append(junctionpoints)
        # no need for updated CC index (?)
        # updated_CC = [node for node in CC if node not in junctionpoints] 
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
            # equidpoints = find_equidpoints(CSC, D)
    #         V.append(equidpoints)
    # for u in V:
    #     # V_t = V[w for w in V if w != u]
    #     for v in V:
    #         if are_neighbours(u,v):
    #             E.append([u,v])
    # print(neigh_img)
    return V,E
    

paths = ['C:/Users/Gwenael/Desktop/MT/histograph-master/01_GW/00_WordImages/270-01-02.png',
        'C:/Users/Gwenael/Desktop/MT/histograph-master/01_GW/00_WordImages/270-01-03.png',
        'C:/Users/Gwenael/Desktop/MT/histograph-master/01_GW/00_WordImages/270-01-04.png',
        'C:/Users/Gwenael/Desktop/MT/histograph-master/01_GW/00_WordImages/270-01-05.png' ]

# test_i = np.array([[0, 0, 0, 0, 0],
                   # [0, 255, 255, 255, 0],
                   # [0, 255, 255, 255, 0],
                   # [0, 255, 255, 255, 0],
                   # [0, 0, 0, 0, 0]],dtype='uint8')

# test_c = [[0,4],[1,1],[2,0],[2,2],[3,4],[4,4]]
# a,b = keypoint(test_i,0)

img = cv.imread(paths[2], 2)
a,b = keypoint(img, 0)
