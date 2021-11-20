# Android Video Magnification
A native C++ Android application that implements an [optimized Eulerian Video Magnification algorithm](https://github.com/kisung5/evm_c_plusplus). 

## Abstract
The extraction of vital signs from videos is possible using the Eulerian Video Magnification. This algorithm has been optimized, creating the opportunity to implement it on mobile devices. In this project, a mobile application that allows the optimized magnification algorithm to be executed on Android devices is implemented. In addition, vital signs are extracted from the processed video, with an average relative error lower than 1.83%. The results also show that smartphone resources are used more efficiently, improving execution times up to a 43.64%, and memory consumption up to 608.65%, compared to the original algorithm executed in personal computers.

## Design
### Process
![image](https://user-images.githubusercontent.com/31488944/142719924-bc42a975-9f46-43ea-bc65-63b63c85258c.png)

### Use case
![image](https://user-images.githubusercontent.com/31488944/142719936-713170b2-6da0-48cb-ad29-0b5b37dd53be.png)

### Components
![image](https://user-images.githubusercontent.com/31488944/142719942-fd265d6d-3e42-4ef3-aea0-63ff4f6762ec.png)

### Packages
![image](https://user-images.githubusercontent.com/31488944/142719947-6cca46c7-3ecb-4d3d-8d9c-5e6d8afe0ade.png)

### Architecture
![image](https://user-images.githubusercontent.com/31488944/142719961-dbd97370-2d96-41c3-ac8f-e33194365c34.png)

### GUI
![image](https://user-images.githubusercontent.com/31488944/142719967-b862fe07-ac4b-4284-b29d-d466049e0f2a.png)

## Algorithms
### Heart rate
![image](https://user-images.githubusercontent.com/31488944/142719980-6c4a5be5-b76a-4f5d-951f-3875b55a9a09.png)

### Breath rate
![image](https://user-images.githubusercontent.com/31488944/142719984-1d7992a3-1c4e-472d-9627-3b2bcaac50e4.png)

## Results
### Heart rate
#### Error
![image](https://user-images.githubusercontent.com/31488944/142719991-2b93b327-e3ce-46c3-baac-7a8d7593b6b5.png)

#### Execution times
![image](https://user-images.githubusercontent.com/31488944/142720003-21502dac-98e7-492b-95d9-d6c2b1673433.png)

#### Memory consumption
![image](https://user-images.githubusercontent.com/31488944/142720005-df36a520-8895-474f-b756-5ee853306515.png)

### Respiratory rate
#### Error
The error is 0%, since all breaths are successfully detected.

#### Execution times
![image](https://user-images.githubusercontent.com/31488944/142720048-cdecf654-9f6c-4c19-8dd3-3f7fc4f7d520.png)

#### Memory consumption
![image](https://user-images.githubusercontent.com/31488944/142720077-de60ba69-cbb7-4577-b3d5-017f234ace28.png)

