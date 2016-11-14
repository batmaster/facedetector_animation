package com.google.android.gms.samples.vision.face.facetracker.ui.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by batmaster on 11/10/2016 AD.
 */

public class ClonableRelativeLayout extends RelativeLayout implements Cloneable {

    public ClonableRelativeLayout(Context context) {
        super(context);
    }

    public ClonableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClonableRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ClonableRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        }
        catch(CloneNotSupportedException e) {
            return null;
        }
    }
}
