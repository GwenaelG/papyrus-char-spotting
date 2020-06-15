# -*- coding: utf-8 -*-
"""
Created on Mon Jun  8 12:11:25 2020

@author: Gwenael
"""
import time 
from keypoint_graph_creation import keypoint_start
from contour_graph_creation import contour_start


print('Contour Graphs')
location = 'C:/Users/Gwenael/Desktop/MT/histograph-master/01_GW/00_WordImages/'
D_list = [2,3,4,5,6,8,10,12]
D = 1
v_list = [0.5, 1, 2, 3, 4]
for val in v_list:
    start = time.time()
    contour_start(location, D, v = val)
    end = time.time()
    print(f'D = {D}, v = {val},  time =  {(end-start):.5} s')