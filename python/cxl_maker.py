# -*- coding: utf-8 -*-
"""
Created on Wed May  6 11:37:09 2020

@author: Gwenael
"""

import time
import sys
import os
import glob
import cv2 as cv

def make_class_txt(gxl_folder, dest_folder):
    name_txt = dest_folder + "bb_comp.txt"
    file_txt = open(name_txt, 'w')
    fileset = glob.glob(gxl_folder+'*.gxl')
    for f in fileset:
        name = f.split(sep = "\\")[-1][:-4]
        bb_class = name.split(sep = "_")[1]
        line = name+" "+bb_class+"\n"
        file_txt.write(line)
    file_txt.close()
        
        
        
        
def make_cxl_from_txt(graph_class_list, file_name, dest_dir):
    valid_file_name = os.path.join(dest_dir,  file_name)
    header_lines = ['<?xml version="1.0"?>\n', '<GraphCollection>\n','<graphs>\n' ]
    footer_lines = ['</graphs>\n', '</GraphCollection>\n']

    file_h = open(valid_file_name, 'w')
    file_h.writelines(header_lines)
    with open(graph_class_list) as rd:
        for n, line in enumerate(rd):
            infos = line.split(" ")
            string = '<print file="{0}.gxl" class="{1}"/>\n'.format(infos[0], infos[1][:-1])
            file_h.write(string)
    file_h.writelines(footer_lines)
    file_h.close()


def make_gt_for_bb(graph_class_list, dest_dir, png_dir):
    if not os.path.exists(dest_dir):
        os.makedirs(dest_dir)
    with open(graph_class_list) as rd:
        for n, line in enumerate(rd):
            infos = line.split(" ")
            file_id, file_class = infos[0], infos[1][:-1]
            file_name = os.path.join(dest_dir, file_id+".xml")
            file_xml = open(file_name, "w")
            file_xml.write('<?xml version="1.0" encoding="UTF-8"?>\n')
            file_xml.write('<papyrus id="{0}">\n'.format(file_id))
            if file_class == 'char':
                file_xml.write('\t<box char="epsilon" id="{0}_1">\n'.format(file_id))
                img_name = png_dir + file_id + ".png"
                bb_img = cv.imread(img_name, 0)
                bb_h , bb_w = bb_img.shape
                coords = ['\t\t<x1>0</x1>\n','\t\t<y1>0</y1>\n','\t\t<x2>{0}</x2>\n'.format(bb_w -1),'\t\t<y2>{}</y2>\n'.format(bb_h - 1)]
                file_xml.writelines(coords)
                file_xml.write('</box>\n')
            file_xml.write('</papyrus>\n')
            file_xml.close()

graph_list_1 = "C:/Users/Gwenael/Desktop/MT/histograph-master/03_AK/00_GroundTruth/02_Test/queries.txt"
graph_list_2 = "C:/Users/Gwenael/Desktop/MT/histograph-master/03_AK/00_GroundTruth/02_Test/words.txt"
dest_dir = "C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/java_GED-master/test/kws_experiment/ak/complete/"
cxl_1 = "queries.cxl"
cxl_2 = "words.cxl"

dest_folder = "C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/java_GED-master/test/papyrus/bb_comp/"
gxl_folder = "C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/files/graphs/contour/gxl/boxes/D_2/v_0/"
§§§§png_folder = 'C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/files/original_bin/boxes/bin_03/'

# make_txt_class(gxl_folder, dest_folder)
# make_cxl_from_txt(dest_folder+"bb_comp.txt", "bb_comp.cxl", dest_folder)
make_gt_for_bb(dest_folder+"bb_comp.txt", gt_folder, png_folder)
