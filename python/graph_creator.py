# -*- coding: utf-8 -*-
"""
Created on Mon Jun  8 12:11:25 2020

@author: Gwenael
"""
import time 
from keypoint_graph_creation import keypoint_start
from contour_graph_creation import contour_start


print('Contour Graphs')
location = 'C:/Users/Gwenael/Desktop/MT/graphs-gwenael/papyrus/original_bin/small_pages/'
D_list = [2,3,4,5,6,8,10,12]
D = 1
v_list = [0.5, 1, 2, 3, 4]
for Di in D_list:
    start = time.time()
    contour_start(location, Di)
    end = time.time()
    print(f'D = {Di}, time =  {(end-start):.5} s')
for vi in v_list:
    start = time.time()
    contour_start(location, D, v=vi)
    end = time.time()
    print(f'D = {D}, v = {vi}, time =  {(end-start):.5} s')