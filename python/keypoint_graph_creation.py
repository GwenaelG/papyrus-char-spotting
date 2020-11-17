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
    finds whether two pixels are neighbours (in the conncted 8-nbhd)

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
            # junctionpoints
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
    # could be no endpoints AND no junction points (??)
    if len(endpoints) == 0:
        #arbitrary junction point at first node
        junctionpoints.append(list(neigh_CC.keys())[0])
    return endpoints, junctionpoints


def add_equidpoints(neigh_CSC, D):
    """
    adds new points every D pixels
    
    TODO: evtl change algo for eqdpt recognition (dont generate neighboring pts)
        maybe dependent on skeleton generation...
    TODO: evtl slide first point such that both ends have approx the same number
        of empty pixels 
    TODO: evtl change CSC generation / junction point recognition to stop CSC
        from being circular and thus having no endpoint 
        problem occurs in such structures:
        x x x
        x   x x x x 
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
        # if only one point in CSC
        if len(neigh_CSC[i]) == 0:
            return []
        # if the node has one nb, we found an endpoint of csc
        if len(neigh_CSC[i]) == 1:
            node = i
            break
    # if no node has one nb, take the first one
    # ie for circular structures
    if node is None:
        node = list(neigh_CSC.keys())[0]
    visited = {node}
    current = {node}
    count = 0
    # visit nodes wave after wave
    # all points with distance n*d are equid points
    while len(visited) < len(neigh_CSC.keys()):
        count += 1
        temp = set()
        for node in current:
            for nb in neigh_CSC[node]:
                if nb not in visited:
                    temp.add(nb)
                    visited.add(nb)
                    if count % D == 0:
                        equidpoints.append(nb)
        current = temp      
    return equidpoints

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
    can display the skeleton with red endpoints, blue junction points and gray 
    equidistant points in a first subplot, and the final graph on top of the 
    word image in the second subplot 
    can save the subplots as images

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
    img_skel = np.full((img.shape[0], img.shape[1],3), 0, dtype='uint8')
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
    img_rgb = cv.cvtColor(img, cv.COLOR_GRAY2RGB)
    black = np.where(img == 0)
    for n in range(len(black[0])):
        img_rgb[black[0][n],black[1][n],:] = (200, 200, 200)
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
        cv.line(img_rgb, (x1, y1), (x2, y2), (150, 150, 150), 1)   
    for node in V[0]:
        pos = coord[node]
        img_rgb[pos[0],pos[1]] = (0,0,0)
    # plt.subplot(2,1,2)
    # plt.imshow(img_rgb)
    location = 'C:/Users/Gwenael/Desktop/MT/graphs-gwenael/GW/keypoint/images/'
    plt.imsave(location+name+'_a.png', img_rgb)
    plt.imsave(location+name+'_b.png', img_skel)
    # plt.tight_layout()
    # plt.show()


def create_gxl(V, E, coord, name, D):
    """
    create a .gxl file containing the name, infos, standard deviations, 
    normalized coordinates of nodes and edges of the graph

    Parameters
    ----------
    V : list
        contains indices of keypoints
    E : set
        edges stored as tuples
    coord : list
        list of coordinates of the skeleton's nodes
    name : string
        name of the graph

    Returns
    -------
    None.

    """
    location = 'C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/files/graphs/keypoint/gxl/chars/D_'+str(D)+'/'
    if not os.path.exists(location):
        os.makedirs(location)
    filename = location+name+'.gxl'
    header_lines = [
        '<?xml version="1.0" encoding="UTF-8"?>\n',
        '<!DOCTYPE gxl SYSTEM "http://www.gupro.de/GXL/gxl-1.0.dtd">\n',
        '<gxl>\n',
        '<graph edgeids="false" edgemode="undirected" id="'+name+'">\n']
    footer_lines = [
        '</graph>\n',
        '</gxl>']
    if len(coord[V]) > 1:
        # normalize coordinates: x_norm = (x - mean_x) / std_x, y_norm = (y - mean_y) / std_y
        # invert x and y! 
        # probably comes from transposition in skeletonize()
        means = np.mean(coord[V], axis = 0)
        stdev = np.std(coord[V], axis = 0)
        max_val = np.amax(coord[V], axis = 0)
        if (stdev[0] != 0) and (stdev[1] != 0):
            file = open(filename, 'w')
            file.writelines(header_lines)
            norm_coord = [[(coord[node][0] - means[0]) / stdev[0], (coord[node][1] - means[1]) / stdev[1]] for node in V]
            string = f'\t<attr name="x_mean">\n\t\t<float>{means[0]}</float>\n\t</attr>\n' \
                f'\t<attr name="y_mean">\n\t\t<float>{means[1]}</float>\n\t</attr>\n' \
                f'\t<attr name="x_std">\n\t\t<float>{stdev[1]}</float>\n\t</attr>\n' \
                f'\t<attr name="y_std">\n\t\t<float>{stdev[0]}</float>\n\t</attr>\n' \
                f'\t<attr name="x_max">\n\t\t<float>{max_val[0]}</float>\n\t</attr>\n' \
                f'\t<attr name="y_max">\n\t\t<float>{max_val[1]}</float>\n\t</attr>\n'
            file.write(string)
            # write all nodes
            for i, node in enumerate(norm_coord):
                string = f'\t<node id="{name}_{i}">\n' \
                    f'\t\t<attr name="x">\n\t\t\t<float>{node[1]}</float>\n\t\t</attr>\n' \
                    f'\t\t<attr name="y">\n\t\t\t<float>{node[0]}</float>\n\t\t</attr>\n' \
                    f'\t</node>\n'
                file.write(string)
            # write all edges
            for edge in E:
                string = f'\t<edge from="{name}_{V.index(edge[0])}" to="{name}_{V.index(edge[1])}"/>\n'
                file.write(string)
            file.writelines(footer_lines)
            file.close()
    
def keypoint_graph(img, D, name):
    """
    transformation from image to graph / image / file

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
    None.

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
    # display_img_graph(img, V, E, skel_coord, name)
    # create gxl file
    create_gxl(V[0], E, skel_coord, name, D)
    

def keypoint_start(location, D, slc = None):
    """
    main function to call, opens the images 

    Parameters
    ----------
    location : String
        folder with images (.png format)
    D : int
        distance between equidistant points
    slc : tuple, optional
        specifies the slice of images to keep. The default is None.

    Returns
    -------
    None.

    """
    fileset = glob.glob(location+'*.png')
    if slc is not None:
        fileset = fileset[slc[0]:slc[1]]
    for n, f in enumerate(fileset):
        img = cv.imread(f,0)
        name = f.split(sep = "\\")[-1][:-4]
        keypoint_graph(img, D, name)



def main():
    print("Run from graph_creator.py")
    
    
if __name__ == '__main__':
    main()
    