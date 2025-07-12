from flask import Flask, request, jsonify
import os
import tempfile
import model as face_model
import predict as face_predict
import base64
import cv2
from http.server import BaseHTTPRequestHandler
import json

# Create Flask app for local development
app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 32 * 1024 * 1024  # 32 MB

# Vercel handler
class VercelHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        if self.path == '/api/train':
            self.handle_train()
        elif self.path == '/api/predict':
            self.handle_predict()
        else:
            self.send_response(404)
            self.end()
    
    def do_GET(self):
        if self.path == '/api/models':
            self.handle_list_models()
        elif self.path == '/api/status':
            self.handle_status()
        else:
            self.send_response(404)
            self.end()
    
    def do_DELETE(self):
        if self.path.startswith('/api/models/'):
            model_name = self.path.split('/')[-1]
            self.handle_delete_model(model_name)
        else:
            self.send_response(404)
            self.end()
    
    def handle_status(self):
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end()
        self.wfile.write(json.dumps({
            'status': 'running',
            'message': 'AIShare API is running on Vercel',
            'limitations': [
                'Training not supported due to file system limitations',
                'Prediction not supported due to file system limitations',
                'Model storage not supported due to file system limitations'
            ],
            'recommendations': [
                'Use Railway for full functionality',
                'Use PythonAnywhere for development',
                'Use cloud VMs for production'
            ]
        }).encode())
    
    def handle_train(self):
        self.send_response(501)
        self.send_header('Content-type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end()
        self.wfile.write(json.dumps({
            'error': 'Training not supported on Vercel',
            'reason': 'Vercel has a read-only file system and cannot save trained models persistently',
            'alternatives': [
                'Railway: Supports persistent storage and all dependencies',
                'PythonAnywhere: Good for development and testing',
                'Cloud VMs: Full control over environment'
            ]
        }).encode())
    
    def handle_predict(self):
        self.send_response(501)
        self.send_header('Content-type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end()
        self.wfile.write(json.dumps({
            'error': 'Prediction not supported on Vercel',
            'reason': 'Vercel cannot handle heavy dependencies like dlib and opencv-python',
            'alternatives': [
                'Railway: Can handle all required dependencies',
                'PythonAnywhere: Supports all face recognition libraries',
                'Cloud VMs: Full control over dependencies'
            ]
        }).encode())
    
    def handle_list_models(self):
        self.send_response(501)
        self.send_header('Content-type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end()
        self.wfile.write(json.dumps({
            'error': 'Model listing not supported on Vercel',
            'reason': 'Vercel has a read-only file system',
            'alternatives': [
                'Railway: Supports persistent file storage',
                'PythonAnywhere: Good for model management',
                'Cloud VMs: Full file system access'
            ]
        }).encode())
    
    def handle_delete_model(self, model_name):
        self.send_response(501)
        self.send_header('Content-type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end()
        self.wfile.write(json.dumps({
            'error': 'Model deletion not supported on Vercel',
            'reason': 'Vercel has a read-only file system',
            'alternatives': [
                'Railway: Supports file operations',
                'PythonAnywhere: Good for model management',
                'Cloud VMs: Full file system access'
            ]
        }).encode())

# Flask routes for local development
@app.route('/train', methods=['POST'])
def train():
    person_name = request.form.get('person_name')
    files = request.files.getlist('images')
    if not person_name or not files:
        return jsonify({'error': 'Missing person_name or images'}), 400

    # Save uploaded images to temp files
    image_paths = []
    for file in files:
        with tempfile.NamedTemporaryFile(delete=False, suffix='.jpg') as temp:
            file.save(temp.name)
            image_paths.append(temp.name)

    # Train and save model
    model_path = face_model.train_and_save_model(image_paths, person_name)
    # Clean up temp files
    for path in image_paths:
        try:
            os.remove(path)
        except Exception as e:
            print(f"Warning: could not delete temp file {path}: {e}")
    if model_path:
        return jsonify({'success': True, 'model_path': model_path})
    else:
        return jsonify({'success': False, 'error': 'Training failed'}), 500

@app.route('/predict', methods=['POST'])
def predict():
    try:
        files = request.files.getlist('images')
        if not files:
            return jsonify({'results': {}})
        models = face_predict.load_models()
        results = {}
        for file in files:
            with tempfile.NamedTemporaryFile(delete=False, suffix='.jpg') as temp:
                file.save(temp.name)
                temp_path = temp.name
            identified_persons, image = face_predict.predict_faces(temp_path, models)
            try:
                os.remove(temp_path)
            except Exception as e:
                print(f"Warning: could not delete temp file {temp_path}: {e}")
            if not identified_persons:
                continue
            # Draw results on the image
            annotated = face_predict.draw_results(image, identified_persons)
            # Encode image as JPEG and then base64
            _, buffer = cv2.imencode('.png', annotated)
            img_base64 = base64.b64encode(buffer).decode('utf-8')
            # Add the image under ALL detected names
            for person in identified_persons:
                person_name = person[0]
                if person_name not in results:
                    results[person_name] = []
                results[person_name].append(img_base64)
        return jsonify({'results': results})
    except Exception as e:
        print(f"Error in /predict: {e}")
        # Always return a consistent structure
        return jsonify({'results': {}, 'error': str(e)}), 500

@app.route('/models', methods=['GET'])
def list_models():
    # List all model files in face_models directory
    model_dir = "face_models"
    if not os.path.exists(model_dir):
        return jsonify([])
    model_files = [f for f in os.listdir(model_dir) if f.endswith('_model.pkl')]
    return jsonify(model_files)

@app.route('/models/<model_name>', methods=['DELETE'])
def delete_model(model_name):
    model_dir = "face_models"
    model_path = os.path.join(model_dir, model_name)
    if not os.path.exists(model_path):
        return '', 404
    try:
        os.remove(model_path)
        return '', 204
    except Exception as e:
        print(f"Error deleting model {model_name}: {e}")
        return '', 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000) 