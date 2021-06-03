# -*- coding: utf-8 -*-
"""
Created on Tue Jun  9 11:26:42 2020

@author: Gwenael
"""

import glob
import numpy as np
import cv2 as cv
import matplotlib.pyplot as plt

def sort_occurences(file):
    with open(file, 'r') as rd:
        lines = rd.readlines()
        dic = {}
        for i,l in enumerate(lines):
            dic.update({l.split(' ')[1] : int(l.split(' ')[2].strip('\n'))})
        sorted_dic = {}
        sorted_keys = sorted(dic, key=dic.get, reverse = True)
        for k in sorted_keys:
            sorted_dic[k] = dic[k]
        wr_file = file[:-4]+"_sorted.txt"
        with open(wr_file, 'w') as wr:
            for k,v in sorted_dic.items():
                wr.write(k+" "+str(v)+"\n")

def dummy_graphs(sizes):
    location = 'C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/files/graphs/dummy/'
    header_lines = [
        '<?xml version="1.0" encoding="UTF-8"?>\n',
        '<!DOCTYPE gxl SYSTEM "http://www.gupro.de/GXL/gxl-1.0.dtd">\n',
        '<gxl>\n',
        '<graph edgeids="false" edgemode="undirected" id="dummy">\n',
        '\t<attr name="x_mean">\n\t\t<float>0</float>\n\t</attr>\n',
        '\t<attr name="y_mean">\n\t\t<float>0</float>\n\t</attr>\n',
        '\t<attr name="x_std">\n\t\t<float>0</float>\n\t</attr>\n',
        '\t<attr name="y_std">\n\t\t<float>0</float>\n\t</attr>\n',
        '\t<attr name="x_max">\n\t\t<float>0</float>\n\t</attr>\n',
        '\t<attr name="y_max">\n\t\t<float>0</float>\n\t</attr>\n']
    footer_lines = [
        '</graph>\n',
        '</gxl>']
    for s in sizes:
        filename = location+"dummy"+str(s)+".gxl"
        file = open(filename, 'w')
        file.writelines(header_lines)
        for i in range(s):
             node_string = f'\t<node id="node_{i}">\n' \
                 '\t\t<attr name="x">\n\t\t\t<float>0</float>\n\t\t</attr>\n' \
                 '\t\t<attr name="y">\n\t\t\t<float>0</float>\n\t\t</attr>\n' \
                 '\t</node>\n'
             file.write(node_string)
        for i in range(s):
             edge_string = f'\t<edge from="node_0" to="node_{i}"/>\n'
             file.write(edge_string)
        file.writelines(footer_lines)
        file.close()
    location_cxl = 'C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/java_GED-master/test/papyrus/cxl/'
    header_lines = ['<?xml version="1.0" encoding="UTF-8"?>\n','<GraphCollection>\n','<graphs>\n']
    footer_lines = ['</graphs>\n','</GraphCollection>']
    for s in sizes:
        filename = location_cxl+"dummy"+str(s)+".cxl"
        file = open(filename, 'w')
        file.writelines(header_lines)
        file.write(f'<print class="page" file="dummy{s}.gxl"/>\n')
        file.writelines(footer_lines)
        file.close()
            
    

def find_best_result(loc):
    with open(loc+'best_result.txt','w') as wr:
        wr.write(loc)
        best_map = 0
        fileset=glob.glob(loc+'*/trec_global')
        maps = []
        for f in fileset:
            with open(f, 'r') as rd:
                lines = rd.readlines()
                MAP = float(''.join(lines[6].replace(chr(0),chr(32)).split(' '))[-7:])
                maps.append(MAP)

        maxMAP = max(maps)
        best = [fileset[i].split('\\')[-2] for i,m in enumerate(maps) if m == maxMAP]
        print(best)
        print(maxMAP)
        print("\n")
        return best, maxMAP
    
def csv_recall_precision(folder):
    loc_csv = 'C:/Users/Gwenael/Desktop/MT/latex/data/recall_precision/'
    num = folder.split('/')[-2]
    recall = np.arange(0,11)
    with open(folder+num+'.res','r') as rd:
        name = '_'.join(rd.readlines()[9].split('/')[-1].split('.')[0].split('_')[1:])
        with open(loc_csv+num+'_'+name+'.csv', 'w') as wr:
            wr.write('recall, precision\n')
            with open(folder+"trec_global", 'r') as rd:
                lines = rd.readlines()
                for i,n in enumerate(range(14,35,2)):
                   l = ''.join(lines[n].replace(chr(0),chr(32)).split(' '))
                   wr.write(str(recall[i]/10)+', '+l[-7:])
               
                
def upscale(loc, f):
    fileset = glob.glob(loc+'*.png')
    for file in fileset:
        img = cv.imread(file, 0)
        name = file.split(sep = "\\")[-1][:-4]
        img = cv.imread(file,0)
        img = cv.resize(img, (0,0), fx=f, fy=f, interpolation=cv.INTER_NEAREST)
        plt.imsave(loc+name+'_up.png',img, cmap='gray')
                
    
loc = 'C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/files/good_graph_images/bin/'
upscale(loc,6)