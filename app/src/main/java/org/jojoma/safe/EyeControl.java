package org.jojoma.safe;

import java.util.ArrayList;

/**
 * Created by cokelas on 7/10/16.
 */

public class EyeControl {

    public State stateHorizontal;
    public State stateVertical;
    private ArrayList<Integer> valueListHorizontal;
    private ArrayList<Integer> valueListVertical;
    public Double meanHorizontal;
    public Double meanVertical;



    public EyeControl(){
        stateHorizontal = null;
        stateVertical = null;
        valueListHorizontal = new ArrayList<>();
        valueListVertical = new ArrayList<>();
        meanHorizontal = 0.0;
        meanVertical = 0.0;
    }

    public void addValueHorizontal(int v){
        if(valueListHorizontal.size() >= CameraPreviewActivity.TIMES_TO_CALIBRATE){
            valueListHorizontal.remove(0);
        }
        valueListHorizontal.add(v);
    }

    public void addValueVertical(int v){
        if(valueListVertical.size() >= CameraPreviewActivity.TIMES_TO_CALIBRATE){
            valueListVertical.remove(0);
        }
        valueListVertical.add(v);
    }

    public void resetListValue(){
        valueListHorizontal = new ArrayList<>();
        valueListVertical = new ArrayList<>();
        meanHorizontal = 0.0;
        meanVertical = 0.0;
    }

    public void updateMeanHorizontal(){
        double sum = 0.0;
        for (int d : valueListHorizontal){
            sum += d;
        }
        meanHorizontal = sum/valueListHorizontal.size();
    }

    public void updateMeanVertical(){
        double sum = 0.0;
        for (int d : valueListVertical){
            sum += d;
        }
        meanVertical = (double) sum/valueListVertical.size();
    }

    public double getMeanHorizontal(){
        double sum = 0.0;
        for (int d : valueListHorizontal){
            sum += d;
        }
        return sum/valueListHorizontal.size();
    }

    public double getMeanVertical(){
        double sum = 0.0;
        for (int d : valueListVertical){
            sum += d;
        }
        return (double) sum/valueListVertical.size();
    }

    public void updateState(){
        if (getMeanHorizontal() > meanHorizontal+3){
            stateHorizontal = State.RIGHT_AIM;
        }
        else if (getMeanHorizontal() < meanHorizontal-3){
            stateHorizontal = State.LEFT_AIM;
        }
        if (getMeanVertical() > meanVertical+3){
            stateVertical = State.UP_AIM;
        }
        else if (getMeanVertical() < meanVertical-3){
            stateVertical = State.DOWN_AIM;
        }
    }



}
