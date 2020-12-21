# -*- coding: utf-8 -*-
"""
Created on Mon Jun  8 12:11:25 2020

@author: Gwenael
"""
import time 
from keypoint_graph_creation import keypoint_start
from contour_graph_creation import contour_start


location = 'C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/files/original_bin/gw/pages/'

def contour():
    print('Contour Graphs')
    D_list = [2,3,4,5,6,8,10,12]
    D = 1
    v_list = [2, 0.5, 1, 3, 4]
    for vi in v_list:
        start = time.time()
        print(f'start: D = {D}, v = {vi}')
        contour_start(location, D, v=vi)
        end = time.time()
        print(f'\t end: time =  {(end-start):.5} s')
    for Di in D_list:
        start = time.time()
        print(f'start: D = {Di}')
        contour_start(location, Di)
        end = time.time()
        print(f'\t end: time =  {(end-start):.5} s')
    
    
def keypoint():
    print('Keypoint Graphs')
    D_list = [5,6,2,3,4,8,10,12]
    for Di in D_list:
        start = time.time()
        print(f'start: D = {Di}')
        keypoint_start(location, Di)
        end = time.time()
        print(f'\t end: time =  {(end-start):.5} s')
    

#contour()
keypoint()