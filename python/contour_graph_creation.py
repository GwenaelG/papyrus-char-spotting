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

# white object on black BG
# cv approxPolyDP

def get_contours(img):
    """
    gets 

    Parameters
    ----------
    img : TYPE
        DESCRIPTION.

    Returns
    -------
    cont : TYPE
        DESCRIPTION.

    """
    t, img = cv.threshold(img, 200, 1, cv.THRESH_BINARY_INV)
    img, cont, h = cv.findContours(img, cv.RETR_TREE, cv.CHAIN_APPROX_NONE)
    return cont

def fill_nodes_edges(cont,d):
    V_i = set()
    E_i = set()
    L = len(cont)
    pts =  np.arange(0, L, d)
    for node in pts:
        V_i.add(node)
    E_i.add(tuple(sorted((pts[0],pts[-1]))))
    for n in range(len(pts)-1):
        E_i.add(tuple(sorted((pts[n],pts[n+1]))))
    return V_i, E_i


def display_img_graph(img, contours, V, E, name):
    img_blank = np.full((img.shape[0],img.shape[1]), 0, dtype='uint8')
    img_cont = cv.drawContours(img_blank, contours, -1, 255, 1)
    img_rgb = cv.cvtColor(img, cv.COLOR_GRAY2RGB)
    black = np.where(img == 0)
    for n in range(len(black[0])):
        img_rgb[black[0][n],black[1][n],:] = (200, 200, 200)
    for i in E:
        for edge in E[i]:
            n1 = edge[0]
            x1 = contours[i][n1][0]
            y1 = contours[i][n1][1]
            n2 = edge[1]
            x2 = contours[i][n2][0]
            y2 = contours[i][n2][1]
            cv.line(img_rgb, (x1, y1), (x2, y2), (150, 150, 150), 1)
    for i in V:
        for node in V[i]:
            pos = contours[i][node]
            img_rgb[pos[1],pos[0]] =(0,0,0)
    plt.subplot(2,1,1),plt.imshow(img_cont, cmap='gray')     
    plt.subplot(2,1,2), plt.imshow(img_rgb)
    plt.tight_layout()
    plt.show()
    # location = 'C:/Users/Gwenael/Desktop/MT/graphs-gwenael/GW/contour/images/'
    # plt.imsave(location+name+'_a.png', img_rgb)
    # plt.imsave(location+name+'_b.png', img_cont, cmap='gray')
    
    
def create_gxl(contours, V, E, name):
    location = 'C:/Users/Gwenael/Desktop/MT/graphs-gwenael/GW/contour/gxl/'
    filename = location+name+'.gxl'
    header_lines = [
        '<?xml version="1.0" encoding="UTF-8"?>\n',
        '<!DOCTYPE gxl SYSTEM "http://www.gupro.de/GXL/gxl-1.0.dtd">\n',
        '<gxl>\n',
        '<graph edgeids="false" edgemode="undirected" id="'+name+'">\n']
    footer_lines = [
        '</graph>\n',
        '</gxl>']
    nodes = []
    for i in V:
        for node in V[i]:
            nodes.append(contours[i][node])
    means
    

    
def contour_graph(img, d, name):
    contours = get_contours(img)
    c = []
    for cont in contours:
        c.append(cont[:,0])
    contours = c
    V = {}
    E = {}
    for i, cont in enumerate(contours):
        # strip unnecessary layer of array
        V_i, E_i = fill_nodes_edges(cont,d)
        V[i] = V_i
        E[i] = E_i
    # display_img_graph(img, contours, V, E, name)
    create_gxl(contours, V, E, name)
    
    
location = 'C:/Users/Gwenael/Desktop/MT/histograph-master/01_GW/00_WordImages/'
fileset = glob.glob(location+'*.png')
d = 6
for n, f in enumerate(fileset[0:1]):
    img1 = cv.imread(f, 0)
    name = f[-13:-4]
    contour_graph(img1, d, name)