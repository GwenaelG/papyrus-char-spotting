# -*- coding: utf-8 -*-
"""
Created on Tue Jun  9 11:26:42 2020

@author: Gwenael
"""

import glob


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
        filename = location+"dummy_"+str(s)+".gxl"
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
        file.close

    

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
        print(maps)
        maxMAP = max(maps)
        best = [i for i,m in enumerate(maps) if m == maxMAP]
        print(loc.split('/')[-2])
        print(best)
        print(maxMAP)
        print("\n")
        return best, maxMAP
        
loc2 = 'C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/java_GED-master/test/papyrus/results/gw/cont/the_step3/'
# loc3 = 'C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/java_GED-master/test/papyrus/results/gw/cont/step3/'
best2, val2 = find_best_result(loc2)              
# best3, val3 = find_best_result(loc3)
# double = [x for x in best2 if x in best3]
# print(double)

# dummy_graphs([10,100,1000,10000, 50000])

# l = 'C:/Users/Gwenael/Desktop/'
# file = l+"gt.txt"
# with open(file, 'r') as rd:
#     lines = rd.readlines()
#     with open(l+'cxl.txt','w') as wr:
#         for line in lines:
#             ls = line.split(" ")
#             s = f'<print class="{ls[1][:-1]}" file="{ls[0]}.gxl"/>\n'
#             wr.write(s)









