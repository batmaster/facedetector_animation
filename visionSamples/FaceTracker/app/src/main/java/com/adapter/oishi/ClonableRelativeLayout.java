package com.adapter.oishi;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by batmaster on 12/8/2016 AD.
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
    protected Object clone() {
        try {
            return super.clone();
        }
        catch(CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
