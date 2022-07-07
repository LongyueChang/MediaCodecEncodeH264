package com.example.mediacodecencode.data;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author : longyue
 * @data : 2022/7/5
 * @email : changyl@yunxi.tv
 */
public class MediaConstant {

    public interface MSG{
        String TOPIC = "encode_topic";
        String PACKET = "packet";
        String WIDTH = "width";
        String HEIGHT = "height";
        String FRAMERATE = "framerate";
        String BITRATE = "bitrate";
        String FRAME_DATA = "frame_data";
        String FRAME_LENGTH = "length";

        int START_ENCODE = 1001;
        int STOP_ENCODE = START_ENCODE+1;
        int ON_FRAME = STOP_ENCODE+1;

    }


    private static int yuvqueuesize = 10;

    public static ArrayBlockingQueue<byte[]> YUVQueue = new ArrayBlockingQueue<byte[]>(yuvqueuesize);
}
