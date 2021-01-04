# -*- coding: utf-8 -*-
"""
Created on Tue Jun  9 11:26:42 2020

@author: Gwenael
"""

import glob

loc2 = 'C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/java_GED-master/test/papyrus/results/gw/cont/step2/'
loc3 = 'C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/java_GED-master/test/papyrus/results/gw/cont/step3/'



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
        best = [i for i,m in enumerate(maps) if m == maxMAP]
        print(loc.split('/')[-2])
        print(best)
        print(maxMAP)
        print("\n")
        return best, maxMAP
        
    
best2, val2 = find_best_result(loc2)              
best3, val3 = find_best_result(loc3)

double = [x for x in best2 if x in best3]

    
