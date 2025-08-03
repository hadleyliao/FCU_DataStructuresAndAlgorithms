let images = [];
let currentIndex = 0;

const fileInput = document.getElementById('fileInput');
const imageDisplay = document.getElementById('imageDisplay');
const noImageText = document.getElementById('noImageText');

function chooseImages() {
  fileInput.click();
}

fileInput.addEventListener('change', () => {
  images = Array.from(fileInput.files).filter(file => file.type.startsWith('image/'));
  currentIndex = 0;
  displayImage();
});

function displayImage() {
  imageDisplay.innerHTML = ''; // 清空顯示區
  if (images.length === 0) {
    imageDisplay.textContent = '尚未選取圖片';
    return;
  }

  const img = document.createElement('img');
  img.src = URL.createObjectURL(images[currentIndex]);
  img.onload = () => URL.revokeObjectURL(img.src); // 釋放記憶體
  imageDisplay.appendChild(img);
}

function prevImage() {
  if (images.length === 0) return;
  currentIndex = (currentIndex - 1 + images.length) % images.length;
  displayImage();
}

function nextImage() {
  if (images.length === 0) return;
  currentIndex = (currentIndex + 1) % images.length;
  displayImage();
}
