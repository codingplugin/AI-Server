# AIShare Face Recognition App

## Vercel Deployment Limitations

**⚠️ IMPORTANT: This app cannot be fully deployed on Vercel due to the following limitations:**

### Why Vercel Won't Work:
1. **File System Limitations**: Vercel has a read-only file system, so you cannot save trained models persistently
2. **Heavy Dependencies**: `dlib`, `opencv-python`, and `face_recognition` are too large for Vercel's build limits
3. **Serverless Architecture**: Face recognition requires significant computational resources that don't fit Vercel's serverless model
4. **Timeout Limits**: Vercel has 10-second timeout limits (30 seconds on paid plans), which is insufficient for face recognition processing

### Alternative Hosting Solutions:

#### 1. **Railway** (Recommended)
- Supports persistent file storage
- Can handle heavy dependencies
- Good for Python applications
- Free tier available

#### 2. **PythonAnywhere**
- Python-focused hosting
- Supports all required dependencies
- Free tier available
- Good for development and testing

#### 3. **Heroku**
- Supports Python applications
- Add-ons for persistent storage
- Free tier discontinued, but affordable paid plans

#### 4. **Google Cloud Run**
- Container-based deployment
- Pay-per-use pricing
- Good for variable workloads

#### 5. **AWS EC2 / Google Compute Engine**
- Full control over the environment
- Can handle any dependencies
- More complex setup but most flexible

### Current Vercel Setup:
The current setup will return error messages indicating that the features are not supported on Vercel. This is intentional to prevent confusion.

### Recommended Next Steps:
1. **Use Railway** for deployment (easiest option)
2. **Use PythonAnywhere** for development and testing
3. **Consider cloud VMs** for production use

### Local Development:
To run locally:
```bash
pip install -r requirements.txt
python server.py
```

The app will be available at `http://localhost:5000` 