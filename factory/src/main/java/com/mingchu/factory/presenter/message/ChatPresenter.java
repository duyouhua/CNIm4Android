package com.mingchu.factory.presenter.message;

import android.support.v7.util.DiffUtil;
import android.text.TextUtils;

import com.mingchu.factory.data.helper.MessageHelper;
import com.mingchu.factory.data.message.MessageDataSource;
import com.mingchu.factory.model.api.message.MsgCreateModel;
import com.mingchu.factory.model.db.Message;
import com.mingchu.factory.persistence.Account;
import com.mingchu.factory.presenter.BaseSourcePresenter;
import com.mingchu.factory.utils.DiffUiDataCallback;

import java.util.List;

/**
 * Created by wuyinlei on 2017/6/24.
 *
 * @function 聊天presenter的基础类
 */

public class ChatPresenter<View extends ChatContract.View>
        extends BaseSourcePresenter<Message, Message, MessageDataSource, View>
        implements ChatContract.Presenter {

    protected String mReceiverId;
    protected int mReceiverType;

    public ChatPresenter(View view, MessageDataSource source, String receiverId, int receiverType) {
        super(view, source);

        this.mReceiverId = receiverId;
        this.mReceiverType = receiverType;
    }

    @Override
    public void onDataLoaded(List<Message> messages) {
        View view = getView();
        if (view == null)
            return;

        //拿到老数据
        List<Message> oldItems = view.getRecyclerViewAadpter().getItems();
        //差异计算
        DiffUiDataCallback<Message> callback = new DiffUiDataCallback<>(oldItems, messages);
        final DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        refreshData(result, messages);  //进行界面刷新
    }

    @Override
    public void pushText(String content) {
        if (TextUtils.isEmpty(content))
            return;
        //构建model
        MsgCreateModel model = new MsgCreateModel.Builder()
                .receiver(mReceiverId,mReceiverType)
                .content(content,Message.TYPE_STR)
                .build();
        //发送消息
        MessageHelper.push(model);

    }

    @Override
    public void pushAudio(String path,long time) {
        //发送语音
        if (TextUtils.isEmpty(path))
            return;
        MsgCreateModel model = new MsgCreateModel.Builder()
                .receiver(mReceiverId,mReceiverType)
                .content(path,Message.TYPE_AUDIO)
                .attach(String.valueOf(time)) //把时长转换为我们之前规定的一个额外字段
                .build();
        //发送消息
        MessageHelper.push(model);

    }

    @Override
    public void pushImages(String[] paths) {
        if (paths == null || paths.length == 0 )
            return;

        for (String path : paths) {

            //这个地方要把图片上传到云服务器  然后返回一个真实的外网的图片地址  赋值给path  然后在发送消息


            MsgCreateModel model = new MsgCreateModel.Builder()
                    .receiver(mReceiverId,mReceiverType)
                    .content(path,Message.TYPE_PIC)
                    .build();
            //发送消息
            MessageHelper.push(model);
        }

    }

    @Override
    public boolean rePush(Message message) {

        //确定消息是可以重复发送的
        if (Account.isLogin()&&message.getSender().getId().equalsIgnoreCase(Account.getUserId())
               && message.getStatus() == Message.STATUS_FAILED) {

            //更改状态
            message.setStatus(Message.STATUS_CREATED);
            //构建一个MsgCreateModel
            MsgCreateModel createModel = MsgCreateModel.buildWithMessage(message);

            MessageHelper.push(createModel);
            return true;
        }

        return false;
    }
}
