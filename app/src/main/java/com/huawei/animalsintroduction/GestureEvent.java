package com.huawei.animalsintroduction;

import android.view.MotionEvent;

/**
 * Gesture event management class for storing and creating gestures.
 */
public class GestureEvent {
    /**
     * Define the constant 0, indicating an unknown gesture type.
     */
    public static final int GESTURE_EVENT_TYPE_UNKNOW = 0;

    /**
     * Define the constant 1, indicating that the gesture type is DOWN.
     */
    public static final int GESTURE_EVENT_TYPE_DOWN = 1;

    /**
     * Define the constant 2, indicating that the gesture type is SINGLETAPUP.
     */
    public static final int GESTURE_EVENT_TYPE_SINGLETAPUP = 2;

    /**
     * Define the constant 3, indicating that the gesture type is SCROLL.
     */
    public static final int GESTURE_EVENT_TYPE_SCROLL = 3;

    public static final int GESTURE_EVENT_TYPE_PINCH = 4;

    private int type;

    private MotionEvent eventFirst;

    private MotionEvent eventSecond;

    private float distanceX;

    private float distanceY;

    private GestureEvent() {
    }

    public float getDistanceX() {
        return distanceX;
    }

    public float getDistanceY() {
        return distanceY;
    }

    public int getType() {
        return type;
    }

    public MotionEvent getEventFirst() {
        return eventFirst;
    }

    public MotionEvent getEventSecond() {
        return eventSecond;
    }

    /**
     * Create a gesture type: DOWN.
     *
     * @param motionEvent The gesture motion event: DOWN.
     * @return GestureEvent.
     */
    static GestureEvent createDownEvent(MotionEvent motionEvent) {
        GestureEvent ret = new GestureEvent();
        ret.type = GESTURE_EVENT_TYPE_DOWN;
        ret.eventFirst = motionEvent;
        return ret;
    }


    static GestureEvent createPinchEvent() {
        GestureEvent ret = new GestureEvent();
        ret.type = GESTURE_EVENT_TYPE_PINCH;
        return ret;
    }
    /**
     * Create a gesture type: SINGLETAPUP.
     *
     * @param motionEvent The gesture motion event: SINGLETAPUP.
     * @return GestureEvent(SINGLETAPUP).
     */
    static GestureEvent createSingleTapUpEvent(MotionEvent motionEvent) {
        GestureEvent ret = new GestureEvent();
        ret.type = GESTURE_EVENT_TYPE_SINGLETAPUP;
        ret.eventFirst = motionEvent;
        return ret;
    }

    /**
     * Create a gesture type: SCROLL.
     *
     * @param e1 The first down motion event that started the scrolling.
     * @param e2 The second down motion event that ended the scrolling.
     * @param distanceX The distance along the X axis that has been scrolled since the last call to onScroll.
     * @param distanceY The distance along the Y axis that has been scrolled since the last call to onScroll.
     * @return GestureEvent(SCROLL).
     */
    static GestureEvent createScrollEvent(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        GestureEvent ret = new GestureEvent();
        ret.type = GESTURE_EVENT_TYPE_SCROLL;
        ret.eventFirst = e1;
        ret.eventSecond = e2;
        ret.distanceX = distanceX;
        ret.distanceY = distanceY;
        return ret;
    }
}