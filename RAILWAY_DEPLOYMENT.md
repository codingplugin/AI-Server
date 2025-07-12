# Railway Deployment Guide

## Why Railway is Recommended

Railway is the best choice for your face recognition app because:
- ✅ Supports persistent file storage (for saving trained models)
- ✅ Can handle heavy dependencies (dlib, opencv-python, face_recognition)
- ✅ Good Python support
- ✅ Free tier available
- ✅ Easy deployment process

## Step 1: Prepare Your Repository

1. **Restore the original requirements.txt**:
```bash
# Replace the current requirements.txt with the original one
flask
face_recognition
dlib
cmake
opencv-python
numpy
Pillow
```

2. **Create a Procfile** (if not exists):
```
web: python server.py
```

3. **Update server.py** to use Railway's PORT environment variable:
```python
if __name__ == '__main__':
    port = int(os.environ.get('PORT', 5000))
    app.run(host='0.0.0.0', port=port)
```

## Step 2: Deploy to Railway

1. **Sign up for Railway**:
   - Go to [railway.app](https://railway.app)
   - Sign up with your GitHub account

2. **Create a new project**:
   - Click "New Project"
   - Select "Deploy from GitHub repo"
   - Choose your repository

3. **Configure the deployment**:
   - Railway will automatically detect it's a Python app
   - It will install dependencies from `requirements.txt`
   - The app will be deployed automatically

4. **Get your deployment URL**:
   - Railway will provide a URL like `https://your-app-name.railway.app`
   - Update your Android app's base URL to this new URL

## Step 3: Update Android App

Update your Android app's base URL in the relevant files:

```kotlin
// In your API service or wherever you define the base URL
private const val BASE_URL = "https://your-app-name.railway.app"
```

## Step 4: Test the Deployment

1. **Test the API endpoints**:
   - `GET https://your-app-name.railway.app/models` - Should return empty array initially
   - `POST https://your-app-name.railway.app/train` - Should work for training
   - `POST https://your-app-name.railway.app/predict` - Should work for prediction

2. **Test from your Android app**:
   - Try training a model
   - Try predicting faces
   - Check if models are saved and can be listed

## Railway Features

### Free Tier Limits:
- 500 hours/month
- 512MB RAM
- Shared CPU
- 1GB storage

### Paid Plans:
- $5/month for more resources
- Better performance
- More storage

### Environment Variables:
Railway automatically provides:
- `PORT` - The port your app should listen on
- `RAILWAY_STATIC_URL` - For static files (if needed)

### Logs and Monitoring:
- View logs in Railway dashboard
- Monitor resource usage
- Automatic restarts on crashes

## Troubleshooting

### Build Issues:
If you encounter build issues with dlib:
1. Railway usually handles this automatically
2. If not, contact Railway support

### Memory Issues:
If you get out of memory errors:
1. Upgrade to paid plan
2. Optimize your image processing
3. Reduce image sizes before processing

### Storage Issues:
If you run out of storage:
1. Upgrade to paid plan
2. Implement model cleanup
3. Use external storage (AWS S3, etc.)

## Alternative: PythonAnywhere

If Railway doesn't work, try PythonAnywhere:
1. Sign up at [pythonanywhere.com](https://pythonanywhere.com)
2. Upload your files
3. Install dependencies
4. Configure WSGI file
5. Get your URL and update Android app

## Migration from Vercel

Since Vercel won't work for your app:
1. Deploy to Railway using this guide
2. Update your Android app's base URL
3. Test all functionality
4. Remove Vercel deployment if you had one 