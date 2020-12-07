# -*- coding: utf-8 -*-
"""
Created on Tue Jun  9 11:26:42 2020

@author: Gwenael
"""

import glob


def node_count_pers():
    with open('C:/Users/Gwenael/Desktop/MT/graphs-gwenael/GW/test/node_count_pers.txt','w') as wr:
        # for method in ['keypoint','contour']:
        for method in ['contour']:
            wr.write(method+'\n')
            for v in [0.5,1,2,3,4]:
                wr.write('v = '+str(v)+'\n')
                slc = (0,10)
                location = 'C:/Users/Gwenael/Desktop/MT/graphs-gwenael/GW/'+method+'/gxl/D_1/v_'+str(v)+'/'
                fileset = glob.glob(location+'*.gxl')
                if slc is not None:
                    fileset = fileset[slc[0]:slc[1]]
                for file in fileset:
                    with open(file, 'r') as rd:
                        data = rd.read()
                        nodes = data.count("<node")
                        wr.write(str(nodes)+'\n')
            wr.write('\n')
            

def node_count_MS():
    with open('C:/Users/Gwenael/Desktop/MT/graphs-gwenael/GW/test/node_count_MS.txt','w') as wr:
        wr.write('Keypoint MS\n')
        wr.write('D = 4\n')
        slc = (0,10)
        location = 'C:/Users/Gwenael/Desktop/MT/histograph-master/01_GW/01_Keypoint/'
        fileset = glob.glob(location+'*.gxl')
        if slc is not None:
            fileset = fileset[slc[0]:slc[1]]
        for file in fileset:
            with open(file, 'r') as rd:
                data = rd.read()
                nodes = data.count("<node")
                wr.write(str(nodes)+'\n')



def edge_count_pers():
    with open('C:/Users/Gwenael/Desktop/MT/graphs-gwenael/GW/test/edge_count_pers.txt','w') as wr:
        # for method in ['keypoint','contour']:
        for method in ['contour']:
            wr.write(method+'\n')
            for v in [0.5,1,2,3,4]:
                wr.write('v = '+str(v)+'\n')
                slc = (0,10)
                location = 'C:/Users/Gwenael/Desktop/MT/graphs-gwenael/GW/'+method+'/gxl/D_1/v_'+str(v)+'/'
                fileset = glob.glob(location+'*.gxl')
                if slc is not None:
                    fileset = fileset[slc[0]:slc[1]]
                for file in fileset:
                    with open(file, 'r') as rd:
                        data = rd.read()
                        nodes = data.count("<edge")
                        wr.write(str(nodes)+'\n')
            wr.write('\n')
            

def edge_count_MS():
    with open('C:/Users/Gwenael/Desktop/MT/graphs-gwenael/GW/test/edge_count_MS.txt','w') as wr:
        wr.write('Keypoint MS\n')
        wr.write('D = 4\n')
        slc = (0,10)
        location = 'C:/Users/Gwenael/Desktop/MT/histograph-master/01_GW/01_Keypoint/'
        fileset = glob.glob(location+'*.gxl')
        if slc is not None:
            fileset = fileset[slc[0]:slc[1]]
        for file in fileset:
            with open(file, 'r') as rd:
                data = rd.read()
                nodes = data.count("<edge")
                wr.write(str(nodes)+'\n')                
            
# node_count_pers()
# node_count_MS()
edge_count_pers()
# edge_count_MS()
