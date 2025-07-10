import face_recognition
import pickle
import os
import cv2
import numpy as np

# Directory to store trained models
MODEL_DIR = "face_models"
os.makedirs(MODEL_DIR, exist_ok=True)

def preprocess_image(image):
    """Preprocess image by applying histogram equalization to handle illumination variations.
        Args:image (numpy array): Input image in RGB format
        Returns:numpy array: Preprocessed image"""
    gray = cv2.cvtColor(image, cv2.COLOR_RGB2GRAY)# Convert to grayscale
    equalized = cv2.equalizeHist(gray)# Apply histogram equalization
    return cv2.cvtColor(equalized, cv2.COLOR_GRAY2RGB)    # Convert back to RGB for face_recognition compatibility

def train_and_save_model(image_paths, person_name, user_id=None, username=None, unique_id=None):
    """Train face recognition model for a person and save it.
        Args:
        image_paths (list): List of file paths to person's images
        person_name (str): Name of the person
        user_id (int or str, optional): ID of the user who trained the model
        username (str, optional): Username of the user who trained the model
        unique_id (str, optional): Unique ID of the user who trained the model
        Returns:
        model_path (str) if successful, None otherwise
    """
    encodings = []
    for image_path in image_paths:
        if not os.path.exists(image_path):
            print(f"Image not found: {image_path}")
            continue
        # Load image
        image = face_recognition.load_image_file(image_path)
        # Preprocess image for illumination invariance
        image = preprocess_image(image)
        # Detect faces and compute encodings
        face_locations = face_recognition.face_locations(image, model='hog')
        if not face_locations:
            print(f"No faces found in image: {image_path}")
            continue
        # Select the largest face to avoid secondary faces
        areas = [(bottom - top) * (right - left) for (top, right, bottom, left) in face_locations]
        max_idx = np.argmax(areas)
        primary_face_location = [face_locations[max_idx]]
        face_encodings = face_recognition.face_encodings(image, primary_face_location, num_jitters=50)
        if face_encodings:
            encodings.append(face_encodings[0])
        else:
            print(f"No encodings generated for image: {image_path}")
    if not encodings:
        print(f"No valid faces found for {person_name}")
        return None
    # Save encodings to file in user-specific folder (by username and unique_id)
    if username is not None and unique_id is not None:
        user_dir = os.path.join(MODEL_DIR, f"{username}_{unique_id}")
        os.makedirs(user_dir, exist_ok=True)
        model_path = os.path.join(user_dir, f"{person_name}_model.pkl")
    elif username is not None:
        user_dir = os.path.join(MODEL_DIR, username)
        os.makedirs(user_dir, exist_ok=True)
        model_path = os.path.join(user_dir, f"{person_name}_model.pkl")
    else:
        model_path = os.path.join(MODEL_DIR, f"{person_name}_model.pkl")
    with open(model_path, 'wb') as f:
        pickle.dump(encodings, f)
    print(f"Model for {person_name} saved at {model_path}")
    return model_path

# The following functions are for CLI use only and are not used in the web app
# def select_images():
#     ...
# def get_person_name():
#     ...
# def main():
#     ...

if __name__ == "__main__":
    print("This module is intended to be imported for web usage. CLI is disabled.")