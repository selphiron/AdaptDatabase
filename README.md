# AdaptDatabase

This application will generate a new dataset to train the Neural Network in https://github.com/albertsanchezf/SimraNN from a dataset that uses SimRa's project dataset format (https://github.com/simra-project/dataset)

In order to run it:
- You must change:
  - The value of String[] variable 'datasetpaths' located in AdaptDatabase class, to point to your dataset folders.
  - The value of String variable 'OUTPUTDATASETPATH' located in AdaptDatabase class, to point the desired output folder.

- You should modify:
  - The value of int[] variable 'DISCARTEDINCIDENTS' located in AdaptDatabase class, if you want to discard any types of incidents
  - The value of boolean variable 'BINARYCLASSIFICATION' located in AdaptDatabase class, TRUE if you want a binary classification
  - The value of boolean variable 'TERNARYCLASSIFICATION' located in AdaptDatabase class. If you want a ternary classification (TRUE) you MUST indicate the incident type you want to detect in 'ELEMENTINTERNARYCLASSIFICATION'
  - The value of boolean variable 'EXTRACTION' located in AdaptDatabase class, if you want to generate the output files
  - The value of boolean variable 'USERTAG' located in AdaptDatabase class, if you want to include the user tagged incidents
  - The value of integer variable 'MINNUMBEROFREADINGS' located in AdaptDatabase class, to omit data if the number of readings between 3 seconds doesn't reach X samples.
  - The value of integer variable 'WINDOWFRAME" located in AdaptDatabase class, to define the window length in miliseconds
