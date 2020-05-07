# -*- coding: utf-8 -*-
"""
Created on Wed May  6 11:37:09 2020

@author: Gwenael
"""

import time
import sys
import os


def make_cxl(graph_class_list, cxl_filename, p_working_dir):
#    KWS file test-data.py 
    file_name = cxl_filename
    valid_file_name = os.path.join(p_working_dir,  file_name)
    header_lines = ['<?xml version="1.0"?>\n', '<GraphCollection>\n','<graphs>\n' ]
    footer_lines = ['</graphs>\n', '</GraphCollection>\n']

    file_h = open(valid_file_name, 'w')
    file_h.writelines(header_lines)
    # for graph_filename in graph_filename_list:
    #     file_item_str  = '<print file="{0}" class="A"/>\n'.format(graph_filename)
    #     file_h.write(file_item_str)
    with open(graph_class_list) as rd:
        for n, line in enumerate(rd):
            infos = line.split(" ")
            string = '<print file="{0}.gxl" class="{1}"/>\n'.format(infos[0], infos[1][:-1])
            file_h.write(string)
    file_h.writelines(footer_lines)
    file_h.close()

graph_list_1 = "C:/Users/Gwenael/Desktop/MT/histograph-master/03_AK/00_GroundTruth/02_Test/queries.txt"
graph_list_2 = "C:/Users/Gwenael/Desktop/MT/histograph-master/03_AK/00_GroundTruth/02_Test/words.txt"
p_working_dir = "C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/java_GED-master/test/kws_experiment/ak/complete/"
cxl_1 = "queries.cxl"
cxl_2 = "words.cxl"

make_cxl(graph_list_1, cxl_1, p_working_dir)
make_cxl(graph_list_2, cxl_2, p_working_dir)