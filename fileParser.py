# -*- coding: utf-8 -*-
"""
Created on Sat Apr 23 18:50:20 2022

@author: adria
"""


import re
import sys
from deep_translator import GoogleTranslator
import langid

def splitWord(word):
    listWords = []
    aux = ""
    mayusculas = False
    word = word.replace("_", " ")

    for simpleWord in word.split():
        for letra in simpleWord:
            if letra.islower() or letra.isnumeric():
                if len(aux) >= 2 and mayusculas:
                    listWords.append(aux)
                    aux = ""
                aux = aux + letra
                mayusculas = False
            elif letra.isupper() and not mayusculas:
                if aux != "":
                    listWords.append(aux)
                    aux = "" + letra
                else:
                    aux = aux + letra
                mayusculas = True
            elif letra.isupper() and mayusculas:
                aux = aux + letra
                    
        listWords.append(aux)
        aux = ""
        mayusculas = False
                

    return listWords



def splitWordTranslate( word ):
    if word == "":
        return ""
    word = word.replace(".","")
    word = word[0].upper() + word[1:]
    #splitted = re.findall('[A-Z][^A-Z]*|_', word)
    splitted = splitWord(word)
    #concatenate = ' '.join( splitted ).lower()
    concatenate = ' '.join( splitted ).lower()
    

    pattern = re.compile(r'\b([a-z]) (?=[a-z]\b)', re.I)
    concatenate = re.sub(pattern, r'\g<1>', concatenate)



    translated = concatenate

    if traducir == '1':
        sourceLang = langid.classify(concatenate)
        if sourceLang[0] != language and concatenate != '' :
            translated = GoogleTranslator(source='auto', target = language).translate(concatenate)
        
    firstWord = word
    if translated != '':
        firstWord = translated.split()[0]

    if translated.find(' ') != -1:
        rest = " " + translated[translated.index(' ') + 1:] 
    else:
        rest = ""
        
    return firstWord.lower(),rest.lower()
    

def searchInMapGender( key , vectorKeys , vectorValues):
    
    if key in vectorKeys:
        index = vectorKeys.index(key)
        return vectorValues[index]
    return 'N'

def parserReplace( ):
    
    f_read = open("salida.txt")
    text = f_read.readlines()
    f_read.close()


    
    f_read = open(dir_file)
    doc = f_read.readlines()
    f_read.close()

    tokenAnterior = ""
    finalOutput = ""
    vectorKeys = []
    vectorValues = []

    for line in doc:
        if len( line.split()[2] ) > 3 :
            vectorKeys.append( line.split()[0] )
            vectorValues.append( line.split()[2][2] )

    file = sys.argv[3].replace("#","")
    if "/" in sys.argv[3]:
        aux = sys.argv[3].replace("#","").split("/")
        file = aux[len(aux) - 1]
    file = file+"Verbalized.txt"

    with open( file , 'w') as f_write:

        for line in text:
            line = line.replace("G[T[","G[")
            for token in line.split(" "):
                if "T[" not in str(token) and "G[" not in str(token) and "]" in str(token):
                    finalOutput = finalOutput + " " + ' '.join(splitWordTranslate( str(token).replace("]","") ) ) + "'"
                elif "G[" in str(token): #Debemos comprobar su genero
    
                    firstWord,rest = splitWordTranslate( str(token).replace("G[", "") )
                    
                    genero = searchInMapGender( firstWord, vectorKeys, vectorValues)
                    
                    if( genero == 'F'):
                        finalOutput = finalOutput + " " + str(tokenAnterior).split('_')[1] + " '" + firstWord + rest + "'"
                    elif (genero == 'M'):
                        finalOutput = finalOutput + " " + str(tokenAnterior).split('_')[0] + " '" + firstWord + rest + "'"
                    else:
                        finalOutput = finalOutput + " " + str(tokenAnterior).split('_')[0] + " '" + firstWord + rest + "'"
                
                elif "T[" in str(token): #Debemos solo traducirlo

                    if "_" in tokenAnterior and "[" not in tokenAnterior:
                        finalOutput = finalOutput + " " + tokenAnterior


                    if "]" in str(token):
                        finalOutput = finalOutput + " '" + ' '.join(splitWordTranslate( str(token).replace("T[", "").replace("]","") ) ) + "'"
                    else:
                        finalOutput = finalOutput + " '" + ' '.join(splitWordTranslate( str(token).replace("T[", "").replace("]","") ) )
                    
                    
                elif "_" not in str(token):
                    if str(token) != "." and str(token) != ",":
                        finalOutput = finalOutput + " " + str(token)
                    else:
                        finalOutput = finalOutput + str(token)

                tokenAnterior = token
            
            finalOutput = finalOutput + "\n"



        index = 0
        finalOutput = finalOutput.replace(" ' ","' ").replace("  "," ")
        finalOutput = finalOutput.replace("..",".").replace(":.",":").replace(" '.","'.")
        finalOutput = finalOutput.replace(" ',","',").replace(". .",".").replace("]'","'").replace("_ ","_").replace("- ","-").replace("# ","#")
        finalOutput = finalOutput.replace("'t[","'").replace("]'","'").replace("'g[","'").replace("\n\n","\n")

        print(finalOutput)


        if sys.argv[4] != '  ':
            regulares = sys.argv[4].split(';')
            while index+1 < len(regulares):
                finalOutput = re.sub( regulares[index] , regulares[index+1] , finalOutput )
                index = index + 2

        finalOutput = finalOutput.replace("_"," ")
        f_write.write(finalOutput)



language = sys.argv[1]
dir_file = 'diccionarios/' + language + '_dictionary.txt'
traducir = sys.argv[2]

if len(sys.argv) <= 3:
    print("Ha habio un error en el pase de argumentos: program.py language boolTraducir prefixModel [regularAntes, regularDespues]")
else:
    #print("Fin")
    #cadena = "AmericanaHotPizza"
    #cadena = re.sub( "a/an '(?=[^aeiou])" , "a '" , cadena )
    #print(cadena)
    parserReplace(); 