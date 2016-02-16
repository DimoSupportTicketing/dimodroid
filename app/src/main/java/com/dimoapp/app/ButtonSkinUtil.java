package com.dimoapp.app;

import android.graphics.Color;
import android.widget.Button;


public class ButtonSkinUtil
{

    public static void setButtonSuccess ( Button button )
    {
        button.setText( "Επιτυχης" );
        button.setBackgroundColor( Color.GREEN );
        button.getBackground().setAlpha( 70 );
    }

    public static void setButtonFailure ( Button button )
    {
        button.setText( "Απετυχε" );
        button.setBackgroundColor( Color.RED );
        button.getBackground().setAlpha( 70 );
    }

    public static void setButtonLoading ( Button button )
    {
        button.setText( "Φορτωση..." );
        button.setBackgroundColor( Color.YELLOW );
        button.getBackground().setAlpha( 50 );
    }
}
