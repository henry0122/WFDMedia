package com.example.azure.wdmedia;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * Created by pohan on 2017/7/31.
 */

public class FragmentControl implements KeyInUsernameFragment.KeyListener{


    private KeyInUsernameFragment keyInUsernameFragment;
    
    
    
    public void FragmentControl() {
        keyInUsernameFragment = new KeyInUsernameFragment();
        keyInUsernameFragment.setOnKeyListener(this);
    }
    
    
    @Override
    public void btnOKListener() {
        
        
        
    }

    public interface FragmentListener {
        
    }


}
