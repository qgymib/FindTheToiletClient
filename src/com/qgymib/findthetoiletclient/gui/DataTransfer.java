package com.qgymib.findthetoiletclient.gui;

/**
 * 用于各个组件之间的信息交互。
 * @author qgymib
 *
 */
public class DataTransfer {
    
    /**
     * 为AccountFragment及其子Fragment设计的信息交互接口。
     * @author qgymib
     * @see AccountFragment
     * @see LoginFragment
     * @see SignupFragment
     * @see InfoFragment
     */
    public interface ViewTransferForAccount{
        /**
         * 子Fragment通过ViewID通知父Fragment需要切换的目标Fragment。
         * @param viewID R.layout.fragment_account_xxxx
         */
        public void transAction(int viewID);
    }
}
