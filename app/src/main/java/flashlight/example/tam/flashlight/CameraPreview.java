package flashlight.example.tam.flashlight;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.util.List;

public class CameraPreview extends SurfaceView implements
		SurfaceHolder.Callback{
	public Matrix matrix = new Matrix();
	private LayoutMode mLayoutMode;
	protected List<Size> mPreviewSizeList;
	protected boolean mSurfaceConfiguring = false;
	protected List<Size> mPictureSizeList;
	protected Size mPreviewSize;
	protected Size mPictureSize;
	private int mSurfaceChangedCallDepth = 0;
	private static final String CAMERA_PARAM_ORIENTATION = "orientation";
	private static final String CAMERA_PARAM_LANDSCAPE = "landscape";
	private static final String CAMERA_PARAM_PORTRAIT = "portrait";
	private static final int PICTURE_SIZE_MAX_WIDTH = 2880;
	private static final int PREVIEW_SIZE_MAX_WIDTH = 1920;
	private static final double ASPECT_RATIO = 3.0 / 4.0;
	Activity mActivity;
	public int displayOrientation;
	public int layoutOrientation;
	private int mCameraId;
	public Camera mCamera;
	public static SurfaceHolder mHolder;
	public static enum LayoutMode {
		FitToParent, // Scale to the size that no side is larger than the parent
		NoBlank // Scale to the size that no side is smaller than the parent
	};

	public CameraPreview(Activity activity,int cameraId, LayoutMode mode) {
		super(activity);
		mActivity = activity;
		mHolder = getHolder();
		mHolder.addCallback(this);

		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			if (Camera.getNumberOfCameras() > cameraId) {
				mCameraId = cameraId;
			} else {
				mCameraId = 0;
			}
		} else {
			mCameraId = 0;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			mCamera = Camera.open(mCameraId);
		} else {
			mCamera = Camera.open();
		}
		Parameters params = mCamera.getParameters();
		mPreviewSizeList = params.getSupportedPreviewSizes();
		mPictureSizeList = params.getSupportedPictureSizes();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			determineDisplayOrientation();
			setupCamera();
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();
		} catch (IOException e) {
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Parameters p = mCamera.getParameters();
		mSurfaceChangedCallDepth++;
		doSurfaceChanged(width, height);
		mSurfaceChangedCallDepth--;
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (null == mCamera) {
			return;
		}
		mCamera.setPreviewCallback(null);
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);

		if (width > height * ASPECT_RATIO) {
			width = (int) (height * ASPECT_RATIO + .5);
		} else {
			height = (int) (width / ASPECT_RATIO + .5);
		}

		setMeasuredDimension(width, height);
	}

	private Size determineBestPreviewSize(Parameters parameters) {
		List<Size> sizes = parameters.getSupportedPreviewSizes();

		return determineBestSize(sizes, PREVIEW_SIZE_MAX_WIDTH);
	}
	private Size determineBestPictureSize(Parameters parameters) {
		List<Size> sizes = parameters.getSupportedPictureSizes();
		return determineBestSize(sizes, PICTURE_SIZE_MAX_WIDTH);
	}

	protected Size determineBestSize(List<Size> sizes, int widthThreshold) {
		Size bestSize = null;

		for (Size currentSize : sizes) {
			boolean isDesiredRatio = (currentSize.width / 4) == (currentSize.height / 3);
			boolean isBetterSize = (bestSize == null || currentSize.width > bestSize.width);
			boolean isInBounds = currentSize.width <= PICTURE_SIZE_MAX_WIDTH;

			if (isDesiredRatio && isInBounds && isBetterSize) {
				bestSize = currentSize;
			}
		}

		if (bestSize == null) {
			// listener.onCameraError();

			return sizes.get(0);
		}

		return bestSize;
	}
	public void setupCamera() {
		Parameters parameters = mCamera.getParameters();

		Size bestPreviewSize = determineBestPreviewSize(parameters);
		Size bestPictureSize = determineBestPictureSize(parameters);
		parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
		parameters
				.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
		parameters
				.setPictureSize(bestPictureSize.width, bestPictureSize.height);

		mCamera.setParameters(parameters);
	}

	public void determineDisplayOrientation() {
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		Camera.getCameraInfo(mCameraId, cameraInfo);

		int rotation = mActivity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;

		switch (rotation) {
			case Surface.ROTATION_0:
				degrees = 0;
				break;

			case Surface.ROTATION_90:
				degrees = 90;
				break;

			case Surface.ROTATION_180:
				degrees = 180;
				break;

			case Surface.ROTATION_270:
				degrees = 270;
				break;
		}

		int displayOrientation;

		if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			displayOrientation = (cameraInfo.orientation + degrees) % 360;
			displayOrientation = (360 - displayOrientation) % 360;
		} else {
			displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
		}

		this.displayOrientation = displayOrientation;
		this.layoutOrientation = degrees;

		mCamera.setDisplayOrientation(displayOrientation);
	}


	public boolean isPortrait() {
		return (mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
	}
	protected Size determinePreviewSize(boolean portrait, int reqWidth,
											   int reqHeight) {
		// Meaning of width and height is switched for preview when portrait,
		// while it is the same as user's view for surface and metrics.
		// That is, width must always be larger than height for setPreviewSize.
		int reqPreviewWidth; // requested width in terms of camera hardware
		int reqPreviewHeight; // requested height in terms of camera hardware
		if (portrait) {
			reqPreviewWidth = reqHeight;
			reqPreviewHeight = reqWidth;
		} else {
			reqPreviewWidth = reqWidth;
			reqPreviewHeight = reqHeight;
		}

		// Adjust surface size with the closest aspect-ratio
		float reqRatio = ((float) reqPreviewWidth) / reqPreviewHeight;
		float curRatio, deltaRatio;
		float deltaRatioMin = Float.MAX_VALUE;
		Size retSize = null;
		for (Size size : mPreviewSizeList) {
			curRatio = ((float) size.width) / size.height;
			deltaRatio = Math.abs(reqRatio - curRatio);
			if (deltaRatio < deltaRatioMin) {
				deltaRatioMin = deltaRatio;
				retSize = size;
			}
		}

		return retSize;
	}
	protected Size determinePictureSize(Size previewSize) {
		Size retSize = null;
		for (Size size : mPictureSizeList) {
			if (size.equals(previewSize)) {
				return size;
			}
		}
		// if the preview size is not supported as a picture size
		float reqRatio = ((float) previewSize.width) / previewSize.height;
		float curRatio, deltaRatio;
		float deltaRatioMin = Float.MAX_VALUE;
		for (Size size : mPictureSizeList) {
			curRatio = ((float) size.width) / size.height;
			deltaRatio = Math.abs(reqRatio - curRatio);
			if (deltaRatio < deltaRatioMin) {
				deltaRatioMin = deltaRatio;
				retSize = size;
			}
		}

		return retSize;
	}
	protected boolean adjustSurfaceLayoutSize(Size previewSize,
											  boolean portrait, int availableWidth, int availableHeight) {
		float tmpLayoutHeight, tmpLayoutWidth;
		if (portrait) {
			tmpLayoutHeight = previewSize.width;
			tmpLayoutWidth = previewSize.height;
		} else {
			tmpLayoutHeight = previewSize.height;
			tmpLayoutWidth = previewSize.width;
		}

		float factH, factW, fact;
		factH = availableHeight / tmpLayoutHeight;
		factW = availableWidth / tmpLayoutWidth;
		if (mLayoutMode == LayoutMode.FitToParent) {
			// Select smaller factor, because the surface cannot be set to the
			// size larger than display metrics.
			if (factH < factW) {
				fact = factH;
			} else {
				fact = factW;
			}
		} else {
			if (factH < factW) {
				fact = factW;
			} else {
				fact = factH;
			}
		}

		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this
				.getLayoutParams();

		int layoutHeight = (int) (tmpLayoutHeight * fact);
		int layoutWidth = (int) (tmpLayoutWidth * fact);

		boolean layoutChanged;
		if ((layoutWidth != this.getWidth())
				|| (layoutHeight != this.getHeight())) {
			layoutParams.height = layoutHeight;
			layoutParams.width = layoutWidth;

			this.setLayoutParams(layoutParams); // this will trigger another
			// surfaceChanged invocation.
			layoutChanged = true;
		} else {
			layoutChanged = false;
		}

		return layoutChanged;
	}

	protected void configureCameraParameters(Parameters cameraParams,
											 boolean portrait) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) { // for 2.1 and
			// before
			if (portrait) {
				cameraParams.set(CAMERA_PARAM_ORIENTATION,
						CAMERA_PARAM_PORTRAIT);
			} else {
				cameraParams.set(CAMERA_PARAM_ORIENTATION,
						CAMERA_PARAM_LANDSCAPE);
			}
		} else { // for 2.2 and later
			int angle;
			Display display = mActivity.getWindowManager().getDefaultDisplay();
			switch (display.getRotation()) {
				case Surface.ROTATION_0: // This is display orientation
					angle = 90; // This is camera orientation
					break;
				case Surface.ROTATION_90:
					angle = 0;
					break;
				case Surface.ROTATION_180:
					angle = 270;
					break;
				case Surface.ROTATION_270:
					angle = 180;
					break;
				default:
					angle = 90;
					break;
			}
			mCamera.setDisplayOrientation(angle);
		}

		// mPictureSize = mPictureSizeList.get(0);
		// for(Camera.Size size : mPictureSizeList)
		// {
		// if(mPictureSize.width < size.width){
		// mPictureSize = size;
		// }
		// }

		cameraParams.setPictureSize(mPictureSize.width, mPictureSize.height);
		cameraParams.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
		mCamera.setParameters(cameraParams);
	}
	private void doSurfaceChanged(int width, int height) {
		mCamera.stopPreview();

		Parameters cameraParams = mCamera.getParameters();
		boolean portrait = isPortrait();
		if (!mSurfaceConfiguring) {
			Size previewSize = determinePreviewSize(portrait, width,
					height);
			Size pictureSize = determinePictureSize(previewSize);
			mPreviewSize = previewSize;
			mPictureSize = pictureSize;
			mSurfaceConfiguring = adjustSurfaceLayoutSize(previewSize,
					portrait, width, height);
			if (mSurfaceConfiguring && (mSurfaceChangedCallDepth <= 1)) {
				startCameraPreview();
				return;
			}
		}

		configureCameraParameters(cameraParams, portrait);
		mSurfaceConfiguring = false;

		try {
			startCameraPreview();
		} catch (Exception e) {
//			Log.w(LOG_TAG, "Failed to start preview: " + e.getMessage());

			// Remove failed size
			mPreviewSizeList.remove(mPreviewSize);
			mPreviewSize = null;

			// Reconfigure
			if (mPreviewSizeList.size() > 0) { // prevent infinite loop
				surfaceChanged(null, 0, width, height);
			} else {
			}
		}

		Matrix matrix = new Matrix();
		matrix.postRotate(displayOrientation);
		matrix.postScale(width / 2000f, height / 2000f);
		matrix.postTranslate(width / 2f, height / 2f);
		matrix.invert(this.matrix);
	}


	public synchronized void startCameraPreview() {
//		executedTasksCount = new AtomicInteger(-1);
		determineDisplayOrientation();
		setupCamera();

		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();
		} catch (Exception exception) {
		}
	}
}
