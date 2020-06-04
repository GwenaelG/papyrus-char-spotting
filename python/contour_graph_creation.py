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

def fill_nodes_edges(cont,d):
    """
    fills the nodes and edges of a contour, nodes have distance d following 
    the contour.
    simple algorithm: contour points seem to be given in order, so node indices are
    easy to determine, so are edges (each node to its successor, with one more
    edge for linking start and finish).

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
    pts =  np.arange(0, L, d)
    for node in pts:
        V_i.add(node)
    # supplementary edge
    E_i.add(tuple(sorted((pts[0],pts[-1]))))
    for n in range(len(pts)-1):
        E_i.add(tuple(sorted((pts[n],pts[n+1]))))
    return V_i, E_i


def flatten(contours, Vc, Ec):
    """
    

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
        
    E : TYPE
        DESCRIPTION.

    """
    coords = []
    index = [{} for i in contours]
    E = set()
    ind = 0
    for n, cc in enumerate(Vc):
        for node in Vc[cc]:
            coords.append(contours[n][node]) 
            index[n][node] = ind
            ind += 1
    for n, cc in enumerate(Ec):
        for edge in Ec[cc]:
            n1 = edge[0]
            n2 = edge[1]
            i1 = index[n][n1]
            i2 = index[n][n2]
            E.add((i1, i2))
    return coords, E


def display_img_graph(img, contours, coords, E, name):
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
    plt.subplot(2,1,1),plt.imshow(img_cont, cmap='gray')     
    plt.subplot(2,1,2), plt.imshow(img_rgb)
    plt.tight_layout()
    plt.show()
    # location = 'C:/Users/Gwenael/Desktop/MT/graphs-gwenael/GW/contour/images/'
    # plt.imsave(location+name+'_a.png', img_rgb)
    # plt.imsave(location+name+'_b.png', img_cont, cmap='gray')
    
    
def create_gxl(coords, E, name):
    location = 'C:/Users/Gwenael/Desktop/MT/graphs-gwenael/GW/test/'
    filename = location+name+'.gxl'
    header_lines = [
        '<?xml version="1.0" encoding="UTF-8"?>\n',
        '<!DOCTYPE gxl SYSTEM "http://www.gupro.de/GXL/gxl-1.0.dtd">\n',
        '<gxl>\n',
        '<graph edgeids="false" edgemode="undirected" id="'+name+'">\n']
    footer_lines = [
        '</graph>\n',
        '</gxl>']
    means = np.mean(coords, axis = 0)
    stdev = np.std(coords, axis = 0)
    norm_coords = [[(node[0] - means[0]) / stdev[0], (node[1] - means[1]) / stdev[1]] for node in coords]
    file = open(filename, 'w')
    file.writelines(header_lines)
    string = f'\t<attr name="x_std">\n\t\t<float>{stdev[0]}</float>\n\t</attr>\n' \
        f'\t<attr name="y_std">\n\t\t<float>{stdev[1]}</float>\n\t</attr>\n'
    file.write(string)
    for i, node in enumerate(norm_coords):
        string = f'\t<node id="{name}_{i}">\n' \
            f'\t\t<attr name="x">\n\t\t\t<float>{node[0]}</float>\n\t\t</attr>\n' \
            f'\t\t<attr name="y">\n\t\t\t<float>{node[1]}</float>\n\t\t</attr>\n' \
            f'\t</node>\n'
        file.write(string)
    for edge in E:
        string = f'\t<edge from="{name}_{edge[0]}" to="{name}_{edge[1]}"/>\n'
        file.write(string)
    file.writelines(footer_lines)
    file.close()
    

    
def contour_graph(img, d, name):
    contours = get_contours(img)
    c = []
    # strip unnecessary layer of array
    for cont in contours:
        c.append(cont[:,0])
    contours = c
    Vc = {}
    Ec = {}
    for i, cont in enumerate(contours):
        Vc_i, Ec_i = fill_nodes_edges(cont,d)
        Vc[i] = Vc_i
        Ec[i] = Ec_i
    coords, E = flatten(contours, Vc, Ec)
    # display_img_graph(img, contours, coords, E, name)
    create_gxl(coords, E, name)
    

def eps_comp(img, eps_val, name):
    contours = get_contours(img)
    img_cont = np.full((img.shape[0],img.shape[1]), 0, dtype='uint8')
    cv.drawContours(img_cont, contours, -1, 255, 1)
    plt.subplot(2,2,1),plt.imshow(img_cont, cmap='gray')
    plt.title('Original Contours'), plt.xticks([]), plt.yticks([])
    for i, v in enumerate(eps_val):
        img_appr = np.full((img.shape[0], img.shape[1]), 0, dtype='uint8')
        appr_contours = []
        for c in contours:
            eps = v * cv.arcLength(c, True)
            appr_contours.append(cv.approxPolyDP(c, eps, True))
        cv.drawContours(img_appr, appr_contours, -1, 255, 1)
        plt.subplot(2,2,i+2),plt.imshow(img_appr, cmap='gray')
        plt.title(f'Contours with DP, eps = {v}*len'), plt.xticks([]), plt.yticks([])
    location = 'C:/Users/Gwenael/Desktop/MT/graphs-gwenael/GW/contour/comparison/'
    plt.tight_layout()
    plt.savefig(location+'comp_DP_'+name+'.png')
    plt.close()
    

def d_comp(img, d_val, name):
    contours = get_contours(img)
    c = []
    # strip unnecessary layer of array
    for cont in contours:
        c.append(cont[:,0])
    contours = c
    for k, d in enumerate(d_val):
        Vc = {}
        Ec = {}
        for i, cont in enumerate(contours):
            Vc_i, Ec_i = fill_nodes_edges(cont,d)
            Vc[i] = Vc_i
            Ec[i] = Ec_i
        coords, E = flatten(contours, Vc, Ec)
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
        plt.subplot(2,2,k+1), plt.imshow(img_rgb)
        plt.title(f'Graph, d = {d}, {len(coords)} nodes'), plt.xticks([]),plt.yticks([])
    location = 'C:/Users/Gwenael/Desktop/MT/graphs-gwenael/GW/contour/comparison/'
    plt.tight_layout()
    plt.savefig(location+'comp_d_'+name+'.png')
    plt.close()


location = 'C:/Users/Gwenael/Desktop/MT/histograph-master/01_GW/00_WordImages/'
fileset = glob.glob(location+'*.png')
d = 4
eps_val = [0.01, 0.005, 0.001]
d_val = [4, 6, 8, 10]
for n, f in enumerate(fileset[0:10]):
    img = cv.imread(f, 0)
    name = f[-13:-4]
    eps_comp(img, eps_val, name)