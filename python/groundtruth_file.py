# -*- coding: utf-8 -*-
"""
Created on Fri Nov  6 16:54:26 2020

@author: Gwenael
"""

import glob
import csv

def create_xml(csv_folder, gt_folder):
    doc_name = csv_folder.split("/")[-2]
    gt_name = gt_folder+doc_name+".xml"
    with open(gt_name, 'w') as gt_file:
        gt_file.write('<?xml version="1.0" encoding="UTF-8"?>\n')
        gt_file.write('<papyrus id="{0}">\n'.format(doc_name))
        csv_fileset = glob.glob(csv_folder+"*.csv")
        for csv_path in csv_fileset:
            char = csv_path.split("_")[-1][:-4]
            with open(csv_path, 'r') as csv_file:
                csv_reader = csv.reader(csv_file, delimiter=';')
                for l, row in enumerate(csv_reader):
                    if row[0] != "":
                        gt_file.write('\t<box char="{0}" id="{1}_{2}">\n'.format(char, doc_name, l))
                        gt_file.write('\t\t<x1>{0}</x1>\n'.format(row[4]))
                        gt_file.write('\t\t<y1>{0}</y1>\n'.format(row[5]))
                        gt_file.write('\t\t<x2>{0}</x2>\n'.format(row[6]))
                        gt_file.write('\t\t<y2>{0}</y2>\n'.format(row[7]))
                        gt_file.write('\t</box>\n')
        gt_file.write('</papyrus>\n')
            

        
gt_folder = "C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/files/groundtruth/"
csv_folder = "C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/files/groundtruth/csv/orig_bin_02_patch/"
create_xml(csv_folder, gt_folder)