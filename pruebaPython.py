# -*- coding: utf-8 -*-
"""
Created on Sat May 28 20:11:37 2022

@author: adria
"""

import re

cadena = "A_An 'pizza' can"

finalOutput = re.sub( "A_An '(?=[^aeiou])" , "A '" , cadena )

print(finalOutput)