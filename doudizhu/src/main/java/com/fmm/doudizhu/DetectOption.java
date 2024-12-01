package com.fmm.doudizhu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

public class DetectOption {

    private final OrtEnvironment env;
    private final OrtSession session;
    private final int inputWidth=640;
    private final int inputHeight=640;

    public DetectOption(String modelPath, Context context) throws IOException, OrtException {
        env = OrtEnvironment.getEnvironment();
        session = env.createSession(loadModelFile(context, modelPath), new OrtSession.SessionOptions()); // 使用 byte[] 创建 OrtSession
    }

    public DetectOption(String imagePath, String modelPath, int inputWidth, int inputHeight, Context context) throws OrtException, IOException {
        env = OrtEnvironment.getEnvironment();
        session = env.createSession(loadModelFile(context, modelPath), new OrtSession.SessionOptions()); // 使用 byte[] 创建 OrtSession

        // 加载图片并进行检测
        Bitmap bitmap = BitmapFactory.decodeStream(context.getAssets().open(imagePath));

        String[][] results = detectObjects(bitmap);

        // 打印检测结果
        Log.d("ObjectDetection", "Detection results for " + imagePath + ":");
        for (String[] result : results) {
            Log.d("5555", Arrays.toString(result));
        }
    }




    public String[][] detectObjects(Bitmap bitmap) throws OrtException {
        // 1. 预处理图像
        FloatBuffer inputTensor = preprocessBitmap(bitmap);

        // 2. 创建输入张量
        OnnxTensor input = OnnxTensor.createTensor(env, inputTensor, new long[]{1, 3, inputHeight, inputWidth});

        // 3. 运行推理
        try (OrtSession.Result results = session.run(Map.of("images", input))) {
            // 4. 处理输出
            OnnxValue outputTensor = results.get(0);
            return postprocessOutput(outputTensor);
        }
    }



    private FloatBuffer preprocessBitmap(Bitmap bitmap) {
        // 1. 调整大小和填充
        Bitmap resizedBitmap = letterbox(bitmap);

        int stride = inputWidth * inputHeight;
        int[] bmpData = new int[stride];
        resizedBitmap.getPixels(bmpData, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        FloatBuffer imgData = FloatBuffer.allocate(
                3 * inputWidth * inputHeight
        );
        imgData.rewind();

        for (int i = 0; i < inputWidth; i++) {
            for (int j = 0; j < inputHeight; j++) {
                int idx = inputHeight * i + j;
                int pixelValue = bmpData[idx];
                //Log.d("5555", String.valueOf(pixelValue));
                float r = ((pixelValue >> 16) & 0xFF) / 255f;
                float g = ((pixelValue >> 8) & 0xFF) / 255f;
                float b = (pixelValue & 0xFF) / 255f;

                imgData.put(idx, r);
                imgData.put(idx + stride, g);
                imgData.put(idx + stride * 2, b);
            }
        }

        imgData.rewind();
        return imgData;
    }

    // letterbox 填充方法
    private Bitmap letterbox(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scale = Math.min((float) 640 / width, (float) 640 / height);
        int newW = Math.round(scale * width);
        int newH = Math.round(scale * height);

        // 使用 createScaledBitmap 创建调整大小后的位图
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, newW, newH, true);

        Bitmap output = Bitmap.createBitmap(640, 640, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawColor(Color.BLACK); // 使用黑色填充
        canvas.drawBitmap(resized, (640 - newW) / 2f, (640 - newH) / 2f, null);

        return output;
    }

    private String[][] postprocessOutput(OnnxValue outputTensor) {
        // 将 OnnxValue 转换为 OnnxTensor
        OnnxTensor tensor = (OnnxTensor) outputTensor;

        // 获取张量数据
        float[] output = tensor.getFloatBuffer().array();
        // 解析检测结果
        List<Detection> detections = new ArrayList<>();

        int numBoxes = (int) tensor.getInfo().getShape()[1];

        // 遍历所有边界框
        for (int i = 0; i < numBoxes; i++) {
            // 分隔出单个检测框的数据
            float[] boxData = new float[11];
            System.arraycopy(output, i * 11, boxData, 0, 11);
            // 对置信度进行 Min-Max 归一化
            float confidence = boxData[4];
            int classIndex = 0;
            float maxClassProbability = 0;
            for (int j = 0; j < 6; j++) {
                if (boxData[5 + j] > maxClassProbability) {
                    maxClassProbability = boxData[5 + j];
                    classIndex = j;
                }
            }

            // 创建 Detection 对象并添加到列表
            detections.add(new Detection(classIndex, confidence, boxData[0], boxData[1], boxData[2], boxData[3]));
        }



        // 应用非极大值抑制 (NMS)
        List<Detection> filteredDetections = nonMaxSuppression(detections, 0.05F, 0.5F);

        // 将过滤后的检测结果转换为 String[][] 并返
        String[][] results = new String[filteredDetections.size()][6];
        for (int i = 0; i < filteredDetections.size(); i++) {
            Detection detection = filteredDetections.get(i);
            results[i][0] = String.valueOf(detection.classIndex);
            results[i][1] = String.valueOf(detection.confidence);
            results[i][2] = String.valueOf(detection.x1);
            results[i][3] = String.valueOf(detection.y1);
            results[i][4] = String.valueOf(detection.x2);
            results[i][5] = String.valueOf(detection.y2);
        }
        return results;
    }

    // 非极大值抑制 (NMS) 方法
    // 非极大值抑制 (NMS)
    public static List<Detection> nonMaxSuppression(List<Detection> detections, float confThres, float iouThres) {
        Collections.sort(detections, Comparator.comparing(d -> -d.confidence));
        List<Detection> filteredDetections = new ArrayList<>();

        while (!detections.isEmpty()) {
            Detection currentBox = detections.remove(0);
            if (currentBox.confidence >= confThres) {
                filteredDetections.add(currentBox);

                // 移除与当前框 IoU 大于阈值的框
                detections.removeIf(box -> calculateIOU(currentBox, box) > iouThres);
            }
        }
        return filteredDetections;
    }

    // 计算两个边界框的 IOU
    private static float calculateIOU(Detection box1, Detection box2) {
        // 转换中心点和距离为标准边界框坐标
        float box1_xmin = box1.x1 - box1.x2;
        float box1_ymin = box1.y1 - box1.y2;
        float box1_xmax = box1.x1 + box1.x2;
        float box1_ymax = box1.y1 + box1.y2;

        float box2_xmin = box2.x1 - box2.x2;
        float box2_ymin = box2.y1 - box2.y2;
        float box2_xmax = box2.x1 + box2.x2;
        float box2_ymax = box2.y1 + box2.y2;

        // 计算交集区域的坐标
        float x1 = Math.max(box1_xmin, box2_xmin);
        float y1 = Math.max(box1_ymin, box2_ymin);
        float x2 = Math.min(box1_xmax, box2_xmax);
        float y2 = Math.min(box1_ymax, box2_ymax);

        // 计算交集区域的面积
        float intersectionArea = Math.max(0, x2 - x1) * Math.max(0, y2 - y1);

        // 计算两个边界框的面积
        float box1Area = (box1_xmax - box1_xmin) * (box1_ymax - box1_ymin);
        float box2Area = (box2_xmax - box2_xmin) * (box2_ymax - box2_ymin);

        // 计算IoU
        float iou = intersectionArea / (box1Area + box2Area - intersectionArea);

        return iou;
    }

    // Detection 类，用于存储检测结果
    private static class Detection {
        int classIndex;
        float confidence;
        float x1, y1, x2, y2;

        public Detection(int classIndex, float confidence, float x1, float y1, float x2, float y2) {
            this.classIndex = classIndex;
            this.confidence = confidence;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }


    private float findMinConfidence(float[] output, int numBoxes) {
        float minConfidence = Float.MAX_VALUE;
        for (int i = 0; i < numBoxes; i++) {
            float confidence = output[i * 11 + 4]; // 置信度位于第 5 个位置 (索引为 4)
            if (confidence < minConfidence) {
                minConfidence = confidence;
            }
        }
        return minConfidence;
    }

    // 辅助方法：找到所有检测框置信度的最大值
    private float findMaxConfidence(float[] output, int numBoxes) {
        float maxConfidence = Float.MIN_VALUE;
        for (int i = 0; i < numBoxes; i++) {
            float confidence = output[i * 11 + 4]; // 置信度位于第 5 个位置 (索引为 4)
            if (confidence > maxConfidence) {
                maxConfidence = confidence;
            }
        }
        return maxConfidence;
    }

    /**
     * Loads a model file from the assets folder.
     *
     * @param context       The application context.
     * @param modelFileName The name of the model file.
     * @return The byte array containing the model data.
     * @throws IOException If an error occurs while loading the model file.
     */
    private byte[] loadModelFile(Context context, String modelFileName) throws IOException {
        try (InputStream inputStream = context.getAssets().open(modelFileName)) {
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            return buffer;
        }
    }
}