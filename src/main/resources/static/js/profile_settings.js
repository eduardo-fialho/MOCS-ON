function setupCameraControls(options) {
    const startBtn = document.getElementById(options.startBtnId);
    const captureBtn = document.getElementById(options.captureBtnId);
    const closeBtn = document.getElementById(options.closeBtnId);
    const video = document.getElementById(options.videoId);
    const canvas = document.getElementById(options.canvasId);
    const hiddenInput = document.getElementById(options.hiddenInputId);
    const form = document.getElementById(options.formId);
    if (!startBtn || !captureBtn || !closeBtn || !video || !canvas || !hiddenInput || !form) {
        return;
    }
    let mediaStream = null;

    async function startCamera() {
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            alert('A câmera não está disponível neste dispositivo.');
            return;
        }
        try {
            mediaStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: false });
            video.srcObject = mediaStream;
            video.classList.remove('hidden');
            captureBtn.disabled = false;
            closeBtn.disabled = false;
            captureBtn.classList.remove('opacity-60');
            closeBtn.classList.remove('opacity-60');
        } catch (err) {
            console.error('Erro ao acessar a câmera', err);
            alert('Não foi possível acessar a câmera.');
        }
    }

    function stopCamera() {
        if (mediaStream) {
            mediaStream.getTracks().forEach(track => track.stop());
            mediaStream = null;
        }
        video.classList.add('hidden');
        captureBtn.disabled = true;
        closeBtn.disabled = true;
        captureBtn.classList.add('opacity-60');
        closeBtn.classList.add('opacity-60');
    }

    function capturePhoto() {
        if (!mediaStream) {
            alert('Abra a câmera primeiro.');
            return;
        }
        const trackSettings = mediaStream.getVideoTracks()[0].getSettings();
        canvas.width = trackSettings.width || 640;
        canvas.height = trackSettings.height || 480;
        const context = canvas.getContext('2d');
        context.drawImage(video, 0, 0, canvas.width, canvas.height);
        hiddenInput.value = canvas.toDataURL('image/png');
        stopCamera();
        form.submit();
    }

    startBtn.addEventListener('click', startCamera);
    captureBtn.addEventListener('click', capturePhoto);
    closeBtn.addEventListener('click', stopCamera);
    window.addEventListener('beforeunload', stopCamera);
}

document.addEventListener('DOMContentLoaded', () => {
    setupCameraControls({
        startBtnId: 'avatarStartCameraBtn',
        captureBtnId: 'avatarCaptureBtn',
        closeBtnId: 'avatarCloseCameraBtn',
        videoId: 'avatarCameraPreview',
        canvasId: 'avatarCameraCanvas',
        hiddenInputId: 'avatarCapturedImageInput',
        formId: 'avatarCaptureForm'
    });
});