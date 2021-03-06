package com.nukkad.androidapp.View;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.nukkad.androidapp.Common.Events.EventCartDataInitialized;
import com.nukkad.androidapp.Common.IntentActionStrings;
import com.nukkad.androidapp.Controller.Cart.CartController;
import com.nukkad.androidapp.Controller.Cart.CartAdapter;
import com.nukkad.androidapp.Common.CommonDefinitions;
import com.nukkad.androidapp.Model.CartData;
import com.nukkad.androidapp.R;
import com.nukkad.androidapp.View.Common.BaseActivity;
import com.nukkad.androidapp.View.Common.NavDrawerItemList;
import com.nukkad.androidapp.View.Common.NavigationDrawerCallbacks;

import de.greenrobot.event.EventBus;

public class CartActivity extends BaseActivity implements NavigationDrawerCallbacks {

    // Used to store the last screen title. For use in {@link #restoreActionBar()}.
    private CharSequence mTitle;

    View mFooter = null;
    CartController mCartController;
    boolean mDetailViewInitiated = false;
    Activity mParentActivity;
    CartAdapter mCartAdapter;
    boolean comingFromCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_screen);

        super.onCreateDrawer();

        mParentActivity = this;

        mCartController = CartController.GetInstance();
        if (mCartController.IsCartDataInitialized() == false) {
            EventBus.getDefault().register(this);

            findViewById(R.id.loading_message).setVisibility(View.VISIBLE);
            findViewById(R.id.empty_cart_view).setVisibility(View.INVISIBLE);
            findViewById(R.id.ProductList_Cart).setVisibility(View.INVISIBLE);
        } else {
            findViewById(R.id.loading_message).setVisibility(View.INVISIBLE);
            PopulateViews();
        }
    }

    void PopulateViews () {
        if (mCartController.IsCartEmpty()) {
            findViewById(R.id.empty_cart_view).setVisibility(View.VISIBLE);
            TextView delChargeTV = (TextView)findViewById((R.id.delivery_charge_note_empty_cart));
            delChargeTV.setText(mCartController.GetDeliveryChargeNote());
            findViewById(R.id.ProductList_Cart).setVisibility(View.INVISIBLE);
        } else {
            // Populate Cart List View
            ExpandableListView mCartListELV = (ExpandableListView) findViewById(R.id.cart_products_list);
            final CartAdapter cartAdapter = new CartAdapter(this);
            mCartAdapter = cartAdapter;
            mCartListELV.setAdapter(cartAdapter);

            for (int i = 0; i < cartAdapter.getGroupCount(); i++)
                mCartListELV.expandGroup(i);

            mCartListELV.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                    if (mDetailViewInitiated == false) {
                        CartData.Product selProd = (CartData.Product) cartAdapter.getChild(groupPosition, childPosition);
                        Intent intent = new Intent(mParentActivity, ProductDetailActivity.class);
                        intent.putExtra(IntentActionStrings.PRODUCT_ID, selProd.GetProductId());
                        intent.putExtra(IntentActionStrings.CATEGORY_ID, -1);
                        intent.putExtra(IntentActionStrings.OPTION_SELECTION, selProd.GetProductOptionValueId());
                        mParentActivity.startActivity(intent);

                        mDetailViewInitiated = true;
                    }

                    return false;
                }
            });

            mCartAdapter.UpdateFooter();
        }

        Button shopButton = (Button)findViewById(R.id.shop_button);
        shopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mParentActivity, CategoryActivity.class);
                int[] lineage = new int[1]; lineage[0] = -1;
                intent.putExtra(CategoryActivity.LINEAGE, lineage);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    public void onEvent (EventCartDataInitialized evt) {
        EventBus.getDefault().unregister(this);
        findViewById(R.id.loading_message).setVisibility(View.INVISIBLE);
        findViewById(R.id.ProductList_Cart).setVisibility(View.VISIBLE);

        PopulateViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean ret = super.onCreateOptionsMenu(menu);

        /*MenuItem cart = menu.findItem(R.id.action_cart_open);
        if (cart != null)   // Will not be null when coming from onCreate and Nav Drawer restore
            cart.setVisible(false);
        invalidateOptionsMenu();*/

        return ret;
    }

    @Override
    protected void onResume () {
        super.onResume();

        if (mCartController.IsCartDataInitialized()) {
            if (mCartController.IsCartEmpty() == false) {
                mCartAdapter.EventBusRegister();
                if (mCartController.WasDataSetChanged()) {
                    mCartAdapter.notifyDataSetChanged();
                    mCartAdapter.UpdateFooter();
                    mCartController.SetDataSetChanged(false);
                }
            } else {
                findViewById(R.id.empty_cart_view).setVisibility(View.VISIBLE);
                TextView delChargeTV = (TextView) findViewById((R.id.delivery_charge_note_empty_cart));
                delChargeTV.setText(mCartController.GetDeliveryChargeNote());
                findViewById(R.id.ProductList_Cart).setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mDetailViewInitiated = false;
        mCartController.SetDataSetChanged(false);
        if (mCartAdapter != null)
            mCartAdapter.EventBusUnregister();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
    }

    @Override
    public int getDefaultItemSelectId() {
        return NavDrawerItemList.MY_CART_ITEM_ID;
    }
}
