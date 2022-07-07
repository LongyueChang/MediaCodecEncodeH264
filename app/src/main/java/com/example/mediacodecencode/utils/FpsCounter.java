package com.example.mediacodecencode.utils;

/**
 * @author : longyue
 * @data : 2022/6/10
 * @email : changyl@yunxi.tv
 */
public class FpsCounter {

    private int cnt = 0;
    private int prevCnt = 0;
    private long startTime = 0;
    private long prevTime = 0;
    private float fps = 0;
    private float totalFps = 0;


    public void FpsCounter(){
        reset();
    }


    public synchronized FpsCounter reset() {
        this.prevCnt = 0;
        this.cnt = this.prevCnt;
        this.prevTime = System.nanoTime() - 1L;
        this.startTime = this.prevTime;
        return this;
    }


    public synchronized void count() {
        ++this.cnt;
        if(cnt >= Integer.MAX_VALUE)
            reset();
    }


    public synchronized  FpsCounter update() {
        long t = System.nanoTime();
        this.fps = (this.cnt - this.prevCnt) * 1.0E9f / (t - this.prevTime);
        this.prevCnt = this.cnt;
        this.prevTime = t;
        this.totalFps = this.cnt * 1.0E9f / (t - this.startTime);


        return this;
    }


    public synchronized float getFps() {
        return this.fps;
    }


    public synchronized float getTotalFps() {
        return this.totalFps;
    }
}
