package ti.car;

import static androidx.car.app.model.Action.BACK;

import android.os.Handler;
import android.os.Looper;

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
import androidx.car.app.model.Row;
import androidx.car.app.model.SectionedItemList;
import androidx.car.app.model.Template;

import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.util.TiConvert;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class TiCarScreen extends Screen {
    private static TiCarScreen currentScreen;

    public TiCarScreen(CarContext carContext) {
        super(carContext);
        currentScreen = this;
    }

    public static void createToast(String message) {
        TiCarScreen screen = currentScreen;
        if (screen == null) {
            Log.w("TiCar", "createToast: no car screen available yet");
            return;
        }
        CarToast.makeText(screen.getCarContext(), message, CarToast.LENGTH_LONG).show();
    }

    public static void updateScreen() {
        TiCarScreen screen = currentScreen;
        if (screen != null) {
            new Handler(Looper.getMainLooper()).post(screen::invalidate);
        }
    }

    private Template fallbackTemplate(String text) {
        return new MessageTemplate.Builder(text).setTitle("ti.car").build();
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        // List of templates https://developers.google.com/cars/design/create-apps/apps-for-drivers/templates/overview

        HashMap listData = TiCarModule.listData;
        if (listData == null) {
            // the car app can be launched before the JS side has set any template
            return fallbackTemplate("Loading …");
        }
        String templateType = TiConvert.toString(listData.get("type"), "list");

        if (templateType.equals("message")) {
            MessageTemplate template = new MessageTemplate
                    .Builder(TiConvert.toString(listData.get("text"), " "))
                    .setTitle(TiConvert.toString(listData.get("title"), ""))
                    .build();

            return template;
        } else if (templateType.equals("grid")) {
            try {
                JSONObject data = TiConvert.toJSON(listData);
                JSONArray sections = data.getJSONArray("sections");

                ItemList.Builder itemList = new ItemList.Builder();
                for (int sectionsId = 0; sectionsId < sections.length(); sectionsId++) {
                    JSONObject section = (JSONObject) sections.get(sectionsId);
                    JSONArray items = section.getJSONArray("items");

                    for (int i = 0; i < items.length(); ++i) {
                        JSONObject item = (JSONObject) items.get(i);
                        GridItem.Builder gridItem = new GridItem.Builder();
                        final int sectionIndex = sectionsId;
                        final int itemIndex = i;
                        final String text = item.getString("text");
                        gridItem.setTitle(text)
                                .setImage(CarIcon.APP_ICON)
                                .setOnClickListener(() -> TiCarModule.fireClickEvent(sectionIndex, itemIndex, text));
                        itemList.addItem(gridItem.build());
                    }
                }
                return new GridTemplate.Builder()
                        .setTitle(data.getString("title"))
                        .setHeaderAction(BACK)
                        .setSingleList(itemList.build())
                        .build();
            } catch (Exception ex) {
                Log.e("TiCar", "Error creating grid template", ex);
                return fallbackTemplate("Invalid grid template data");
            }
        } else if (templateType.equals("list")) {
            try {
                JSONObject data = TiConvert.toJSON(listData);
                JSONArray sections = data.getJSONArray("sections");

                ListTemplate.Builder templateBuilder = new ListTemplate.Builder();
                for (int sectionsId = 0; sectionsId < sections.length(); sectionsId++) {
                    JSONObject section = (JSONObject) sections.get(sectionsId);

                    String sectionTitle = section.getString("title");
                    JSONArray items = section.getJSONArray("items");
                    ItemList.Builder rowList = new ItemList.Builder();

                    for (int i = 0; i < items.length(); ++i) {
                        JSONObject item = (JSONObject) items.get(i);
                        final int sectionIndex = sectionsId;
                        final int itemIndex = i;
                        final String text = item.getString("text");
                        rowList.addItem(new Row.Builder()
                                .setTitle(text)
                                .setOnClickListener(() -> TiCarModule.fireClickEvent(sectionIndex, itemIndex, text))
                                .build());
                    }

                    templateBuilder.addSectionedList(SectionedItemList.create(rowList.build(), sectionTitle));
                }
                return templateBuilder.setTitle(data.getString("title")).setHeaderAction(BACK).build();
            } catch (Exception ex) {
                Log.e("TiCar", "Error creating list template", ex);
                return fallbackTemplate("Invalid list template data");
            }
        } else {
            return fallbackTemplate("Unknown template type: " + templateType);
        }
    }
}
