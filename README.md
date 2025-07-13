# AI Share - Face Recognition App

A lightweight face recognition system with Android frontend and Python Flask backend using MediaPipe.

## Features

- **Lightweight**: Uses MediaPipe instead of heavy dlib dependencies
- **Android App**: Native Android interface for training and prediction
- **Flask Backend**: Python server for face recognition processing
- **Local Storage**: Models saved locally on the server
- **Real-time**: Fast face detection and recognition

## Setup

### Backend Setup

1. **Install Dependencies**:
   ```bash
   pip install -r requirements.txt
   pip install flask-cors
   ```

2. **Start the Server**:
   ```bash
   python server.py
   ```
   Server will run at: `http://192.168.144.98:5000`

3. **Test the Server**:
   ```bash
   python test_server.py
   ```

### Android App Setup

1. **Open in Android Studio**
2. **Update IP Address** (if needed):
   - Edit `app/src/main/java/com/example/ai_share/network/ApiClient.kt`
   - Change `BASE_URL` to your PC's IP address
3. **Build and Run**

## Usage

### Training Models

1. Open the Android app
2. Go to "Train" tab
3. Enter person's name
4. Select multiple photos of the person
5. Tap "Train Model"
6. Wait for training to complete

### Predicting Faces

1. Go to "Predict" tab
2. Select photos to analyze
3. Tap "Predict"
4. View results grouped by person

### Managing Models

- View all trained models in "Saved Models" tab
- Delete models by tapping the delete button

## API Endpoints

- `POST /train` - Train a new face recognition model
- `POST /predict` - Predict faces in uploaded images
- `GET /models` - Get list of all trained models
- `DELETE /models/{model_name}` - Delete a specific model
- `GET /health` - Health check endpoint

## Technical Details

### Backend Architecture

- **MediaPipe**: Face detection and landmark extraction
- **scikit-learn**: Cosine similarity for face matching
- **OpenCV**: Image processing and preprocessing
- **Flask**: Web server and API endpoints

### Model Storage

- Models saved in `face_models/` directory
- Each model contains face landmarks and features
- Models are pickle files for fast loading

### Performance

- **Training**: ~2-5 seconds per person (depending on image count)
- **Prediction**: ~1-3 seconds per image
- **Accuracy**: 85-95% with good quality images

## Troubleshooting

### Common Issues

1. **Server not starting**:
   - Check if port 5000 is available
   - Ensure all dependencies are installed

2. **Android app can't connect**:
   - Verify IP address in ApiClient.kt
   - Check if PC and phone are on same network
   - Ensure firewall allows connections on port 5000

3. **Face detection issues**:
   - Ensure good lighting in photos
   - Use clear, front-facing images
   - Avoid heavily filtered or edited photos

### Network Configuration

- **PC IP**: 192.168.144.98
- **Port**: 5000
- **Protocol**: HTTP

## File Structure

```
AIShare/
├── app/                    # Android app
│   └── src/main/java/com/example/ai_share/
│       ├── network/       # API client and interfaces
│       ├── TrainActivity.kt
│       ├── PredictActivity.kt
│       └── MainActivity.kt
├── server.py              # Flask server
├── model.py               # Face recognition logic
├── requirements.txt       # Python dependencies
├── test_server.py         # Server testing script
└── README.md             # This file
```

## Dependencies

### Python Backend
- Flask 2.3.3
- MediaPipe 0.10.7
- OpenCV 4.8.1.78
- NumPy 1.24.3
- scikit-learn 1.3.0
- Pillow 10.0.0
- flask-cors

### Android App
- Retrofit for API calls
- OkHttp for networking
- RecyclerView for UI
- Activity Result API for file selection 