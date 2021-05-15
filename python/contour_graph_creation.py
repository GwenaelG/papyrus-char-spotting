# -*- coding: utf-8 -*-
"""
Created on Sat May 30 10:01:00 2020

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
import random


def get_contours(img):
    """
    gets the contours with OpenCV's function

    Parameters
    ----------
    img : int array
        preprocessed binarized image 
        (white background and black foreground)

    Returns
    -------
    cont : array
        array of arrays, one array for each contour 

    """
    # negative image (black background)
    t, img = cv.threshold(img, 200, 1, cv.THRESH_BINARY_INV)
    img, cont, h = cv.findContours(img, cv.RETR_TREE, cv.CHAIN_APPROX_NONE)
    return cont

def fill_nodes_edges(cont,D):
    """
    fills the nodes and edges of a contour, nodes have distance d following 
    the contour.
    The contour points seem to be given in order for each contour part by the
    OpenCV function.
    Node indices are easy to determine, so are edges 
    (each node to its successor, with one more edge for linking start and finish).

    Parameters
    ----------
    cont : array
        coordinates of one contour
    d : int
        distance between nodes.

    Returns
    -------
    V_i : set
        index of the nodes (in the contour)
    E_i : set
        edges of the contour

    """
    V_i = set()
    E_i = set()
    L = len(cont)
    pts =  np.arange(0, L, D)
    for node in pts:
        V_i.add(node)
    # supplementary edge
    E_i.add(tuple(sorted((pts[0],pts[-1]))))
    for n in range(len(pts)-1):
        E_i.add(tuple(sorted((pts[n],pts[n+1]))))
    return V_i, E_i


def flatten(contours, Vc, Ec):
    """
    creates a unique array with all coordinates (no more separation between different
    contour parts) and a unique set of edges 
    since we have unique indices, so the node set V is not needed anymore

    Parameters
    ----------
    contours : array
        coordinates of all contours
    Vc : dict
        dictionary of sets, index of nodes for each contour
    Ec : dict
        dictionary of sets, index of edges for each contour

    Returns
    -------
    coords : array
        coordinates in one array, no more contour separation
    E : set
        set of edges, using indices of coords

    """
    coords = []
    # used for storing temporary references for edges indexing
    index = [{} for i in contours]
    E = set()
    ind = 0
    # remember that nodes are ordered when produced 
    for n, cc in enumerate(Vc):
        for node in Vc[cc]:
            coords.append(contours[n][node]) 
            index[n][node] = ind
            ind += 1
    # use the correct node index
    for n, cc in enumerate(Ec):
        for edge in Ec[cc]:
            n1 = edge[0]
            n2 = edge[1]
            i1 = index[n][n1]
            i2 = index[n][n2]
            E.add((i1, i2))
    return coords, E


def display_img_graph(img, contours, coords, E, name, D, v=0, mode='n'):
    """
    can display the graph on top of the image in a subplot and the contours in
    a second subplot
    can save the subplots as images

    Parameters
    ----------
    img : array
        binarized preprocessed image
    contours : array
        coordinates of all the contours points
    coords : array
        coordinates of the nodes
    E : set
        edges (indexed for coords)
    name : string
        name of the image

    Returns
    -------
    None.

    """
    img_blank = np.full((img.shape[0],img.shape[1]), 0, dtype='uint8')
    img_cont = cv.drawContours(img_blank, contours, -1, 255, 1)
    img_rgb = cv.cvtColor(img, cv.COLOR_GRAY2RGB)
    black = np.where(img == 0)
    for n in range(len(black[0])):
        img_rgb[black[0][n],black[1][n],:] = (200, 200, 200)
    for edge in E:
        n1 = edge[0]
        x1 = coords[n1][0]
        y1 = coords[n1][1]
        n2 = edge[1]
        x2 = coords[n2][0]
        y2 = coords[n2][1]
        cv.line(img_rgb, (x1, y1), (x2, y2), (150, 150, 150), 1)
    for node in coords:
        img_rgb[node[1],node[0]] =(0,0,0)
    if mode == 'd':
        plt.subplot(2,1,1),plt.imshow(img_cont, cmap='gray')     
        plt.subplot(2,1,2), plt.imshow(img_rgb)
        plt.tight_layout()
        plt.show()
    if mode == 's':
        location = 'C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/files/graph_images/'
        if not os.path.exists(location):
            os.makedirs(location)
        if v != 0:
            plt.imsave(location+name+'_DP_'+str(v)+'.png', img_rgb)
        else:
            plt.imsave(location+name+'_D_'+str(D)+'.png', img_rgb)
        plt.imsave(location+name+'_DP_b.png', img_cont, cmap='gray')
    
    
def create_gxl(coords, E, name, D, v=None):
    """
    create a .gxl file containing the name, infos, standard deviations, 
    normalized coordinates of nodes and edges of the graph

    Parameters
    ----------
    coords : array
        coordinates of the nodes
    E : set
        edges 
    name : string
        name of the graph

    Returns
    -------
    None.

    """
    location = 'C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/files/graphs/contour/gxl/test_cont/'
    if v is not None: 
        location = location + 'v_'+str(v)+'/'
    # location = 'C:/Users/Gwenael/Desktop/MT/graphs-gwenael/papyrus/test/'
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
    if len(coords) > 1:
        means = np.mean(coords, axis = 0)
        stdev = np.std(coords, axis = 0)
        max_val = np.amax(coords, axis = 0)
        # normalize coordinates: x_norm = (x - mean_x) / std_x, y_norm = (y - mean_y) / std_y
        if (stdev[0] != 0) and (stdev[1] != 0):
            file = open(filename, 'w')
            file.writelines(header_lines)
            norm_coords = [[(node[0] - means[0]) / stdev[0], (node[1] - means[1]) / stdev[1]] for node in coords]
            # keep mean, standard deviation, max values 
            string = f'\t<attr name="x_mean">\n\t\t<float>{means[0]}</float>\n\t</attr>\n' \
                f'\t<attr name="y_mean">\n\t\t<float>{means[1]}</float>\n\t</attr>\n' \
                f'\t<attr name="x_std">\n\t\t<float>{stdev[0]}</float>\n\t</attr>\n' \
                f'\t<attr name="y_std">\n\t\t<float>{stdev[1]}</float>\n\t</attr>\n' \
                f'\t<attr name="x_max">\n\t\t<float>{max_val[0]}</float>\n\t</attr>\n' \
                f'\t<attr name="y_max">\n\t\t<float>{max_val[1]}</float>\n\t</attr>\n'
            file.write(string)
            # write all nodes
            for i, node in enumerate(norm_coords):
                string = f'\t<node id="{name}_{i}">\n' \
                    f'\t\t<attr name="x">\n\t\t\t<float>{node[0]}</float>\n\t\t</attr>\n' \
                    f'\t\t<attr name="y">\n\t\t\t<float>{node[1]}</float>\n\t\t</attr>\n' \
                    f'\t</node>\n'
                file.write(string)
            # write all edges
            for edge in E:
                string = f'\t<edge from="{name}_{edge[0]}" to="{name}_{edge[1]}"/>\n'
                file.write(string)
            file.writelines(footer_lines)
            file.close()
    

    
def contour_graph(img, D, name, v=0):
    """
    main function, calls the other ones

    Parameters
    ----------
    img : array
        preprocessed binarized image
    d : int
        distance between pixels   
    name : String
        name of the graph

    Returns
    -------
    None.

    """
    contours = get_contours(img)
    temp_c = []
    for cont in contours:
        appr_cont = cont
        if v != 0:
            # eps = v * cv.arcLength(cont, True)
            eps = v
            appr_cont = cv.approxPolyDP(cont, eps, True)
        temp_c.append(appr_cont[:,0])
    contours = temp_c
    Vc = {}
    Ec = {}
    for i, cont in enumerate(contours):
        Vc_i, Ec_i = fill_nodes_edges(cont,D)
        Vc[i] = Vc_i
        Ec[i] = Ec_i
    coords, E = flatten(contours, Vc, Ec)
    display_img_graph(img, contours, coords, E, name, D, v, 's')
    create_gxl(coords, E, name, D, v) # !change location in fct itself
    

def contour_start(location, D, slc = None, v=0):
    fileset = glob.glob(location+'*.png')
    if slc is not None:
        fileset = fileset[slc[0]:slc[1]]
    for f in fileset:
        img = cv.imread(f, 0)
        name = f.split(sep = "\\")[-1][:-4]
        contour_graph(img, D, name, v)
        
        
def main():
    print("Run from graph_creator.py")
     
if __name__=='__main__':
    main()