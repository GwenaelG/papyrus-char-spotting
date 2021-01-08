# -*- coding: utf-8 -*-
"""
Created on Fri Jan  8 10:37:07 2021

@author: Gwenael
"""

import numpy as np
import cv2 as cv
import matplotlib.pyplot as plt
import glob


x = np.arange(1000, 10001, 1000)
y = np.array([1463, 16792, 57358, 178408, 442565, 808313, 1484170, 2237259, 3996883, 6360825])
z = x ** 2

plt.subplot(1,2,1)
plt.plot(x,y/1000,'.-')
plt.title('Time for graph parsing')
plt.xlabel('Amount of nodes')
plt.ylabel('time (s)')

plt.subplot(1,2,2)
plt.loglog(x,y/1000,'.-')
plt.title('Time for graph parsing (log-log)')
plt.xlabel('Amount of nodes')
plt.ylabel('time (s)')


plt.show()
plt.savefig('C:/Users/Gwenael/Desktop/MT/latex/graph_parsing.png', bbox_inches = 'tight')