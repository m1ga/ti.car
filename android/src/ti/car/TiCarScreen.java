package ti.car;

import static androidx.car.app.model.Action.BACK;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.CarToast;
import androidx.car.app.Screen;
import androidx.car.app.model.CarIcon;
import androidx.car.app.model.GridItem;
import androidx.car.app.model.GridTemplate;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.ListTemplate;
import androidx.car.app.model.MessageTemplate;
import androidx.car.app.model.OnClickListener;
import androidx.car.app.model.Row;
import androidx.car.app.model.SectionedItemList;
import androidx.car.app.model.Template;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiConvert;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class TiCarScreen extends Screen {
    private static CarContext globalCarContext;

    public TiCarScreen(CarContext carContext) {
        super(carContext);
        globalCarContext = carContext;
    }

    public static void createToast(String message) {
        CarToast.makeText(globalCarContext, message, CarToast.LENGTH_LONG).show();
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        // List of templates https://developers.google.com/cars/design/create-apps/apps-for-drivers/templates/overview

        HashMap listData = TiCarModule.listData;
        String templateType = TiConvert.toString(listData.get("type"), "list");

        if (templateType.equals("message")) {
            MessageTemplate template = new MessageTemplate
                    .Builder(TiConvert.toString(listData.get("text"), ""))
                    .setTitle(TiConvert.toString(listData.get("title"), ""))
                    .build();

            return template;
        } else if (templateType.equals("grid")) {
            GridTemplate.Builder templateBuilder = new GridTemplate.Builder();
            try {
                JSONObject data = TiConvert.toJSON(listData);
                JSONArray sections = data.getJSONArray("sections");

                ItemList.Builder itemList = new ItemList.Builder();
                for (int sectionsId = 0; sectionsId < sections.length(); sectionsId++) {
                    JSONObject section = (JSONObject) sections.get(sectionsId);

                    String sectionTitle = section.getString("title");
                    JSONArray items = section.getJSONArray("items");

                    for (int i = 0; i < items.length(); ++i) {
                        JSONObject item = (JSONObject) items.get(i);
                        GridItem.Builder gridItem = new GridItem.Builder();
                        int finalI = i;
                        gridItem.setTitle(item.getString("text"))
                                .setImage(CarIcon.APP_ICON)
                                .setOnClickListener(() -> {
                                    Intent broadcastIntent = new Intent();
                                    broadcastIntent.setAction("click").putExtra("index", finalI);
                                    LocalBroadcastManager.getInstance(TiApplication.getAppCurrentActivity()).sendBroadcast(broadcastIntent);
                                });
                        itemList.addItem(gridItem.build());
                    }
                }
                return templateBuilder.setTitle(data.getString("title"))
                        .setHeaderAction(BACK)
                        .setSingleList(itemList.build())
                        .build();
            } catch (Exception ex) {
                Log.e("TiCar", ex.toString());
                return templateBuilder.setTitle("").build();
            }
        } else if (templateType.equals("list")) {
            ListTemplate.Builder templateBuilder = new ListTemplate.Builder();

            try {
                JSONObject data = TiConvert.toJSON(listData);
                JSONArray sections = data.getJSONArray("sections");

                for (int sectionsId = 0; sectionsId < sections.length(); sectionsId++) {
                    JSONObject section = (JSONObject) sections.get(sectionsId);

                    String sectionTitle = section.getString("title");
                    JSONArray items = section.getJSONArray("items");
                    ItemList.Builder radioList = new ItemList.Builder();

                    for (int i = 0; i < items.length(); ++i) {
                        JSONObject item = (JSONObject) items.get(i);
                        radioList.addItem(new Row.Builder().setTitle(item.getString("text")).build());
                    }

                    ItemList itemList = radioList.setOnSelectedListener(this::onSelected).build();
                    templateBuilder.addSectionedList(SectionedItemList.create(itemList, sectionTitle));
                }
                return templateBuilder.setTitle(data.getString("title")).setHeaderAction(BACK).build();
            } catch (Exception ex) {
                Log.e("TiCar", ex.toString());
                return templateBuilder.setTitle("").build();
            }
        } else {
            return new ListTemplate.Builder().build();
        }
    }

    private void onSelected(int i) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("click").putExtra("index", i);
        LocalBroadcastManager.getInstance(TiApplication.getAppCurrentActivity()).sendBroadcast(broadcastIntent);
    }
}
