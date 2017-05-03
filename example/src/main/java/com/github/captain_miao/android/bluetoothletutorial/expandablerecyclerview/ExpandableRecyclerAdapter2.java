package com.github.captain_miao.android.bluetoothletutorial.expandablerecyclerview;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapterHelper;
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.bignerdranch.expandablerecyclerview.Model.ParentWrapper;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The Base class for an Expandable RecyclerView Adapter
 *
 * Provides the base for a user to implement binding custom views to a Parent ViewHolder and a
 * Child ViewHolder
 *
 * @author Ryan Brooks
 * @version 1.0
 * @since 5/27/2015
 */
public abstract class ExpandableRecyclerAdapter2<PVH extends ParentViewHolder, CVH extends ChildViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ExpandableRecyclerAdapter.ExpandCollapseListener, ParentViewHolder.ParentListItemExpandCollapseListener {

    private static final String EXPANDED_STATE_MAP = "ExpandableRecyclerAdapter.ExpandedStateMap";
    private static final int TYPE_PARENT = 0;
    private static final int TYPE_CHILD = 1;

    protected Context mContext;
    protected List<? extends ParentListItem> mParentItemList;
    protected List<Object> mHelperItemList;
    private ExpandableRecyclerAdapter.ExpandCollapseListener mExpandCollapseListener;
    private List<RecyclerView> mAttachedRecyclerViewPool;

    /**
     * Public constructor for the base ExpandableRecyclerView.
     *
     * @param context
     * @param parentItemList List of all parent objects that make up the recyclerview
     */
    public ExpandableRecyclerAdapter2(Context context, @NonNull List<? extends ParentListItem> parentItemList) {
        super();
        mContext = context;
        mParentItemList = parentItemList;
        mHelperItemList = ExpandableRecyclerAdapterHelper.generateParentChildItemList(parentItemList);
        mAttachedRecyclerViewPool = new ArrayList<>();
    }

    /**
     * Override of RecyclerView's default onCreateViewHolder.
     *
     * This implementation determines if the item is a child or a parent view and will then call
     * the respective onCreateViewHolder method that the user must implement in their custom
     * implementation.
     *
     * @param viewGroup
     * @param viewType
     * @return the ViewHolder that corresponds to the item at the position.
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_PARENT) {
            PVH pvh = onCreateParentViewHolder(viewGroup);
            pvh.setParentListItemExpandCollapseListener(this);
            return pvh;
        } else if (viewType == TYPE_CHILD) {
            return onCreateChildViewHolder(viewGroup);
        } else {
            throw new IllegalStateException("Incorrect ViewType found");
        }
    }

    /**
     * Override of RecyclerView's default onBindViewHolder
     *
     * This implementation determines first if the ViewHolder is a ParentViewHolder or a
     * ChildViewHolder. The respective onBindViewHolders for ParentObjects and ChildObject are then
     * called.
     *
     * If the item is a ParentObject, sets the entire row to trigger expansion if instructed to
     *
     * @param holder
     * @param position
     * @throws IllegalStateException if the item in the list is neither a ParentObject or ChildObject
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object helperItem = getHelperItem(position);
        if (helperItem instanceof ParentWrapper) {
            PVH parentViewHolder = (PVH) holder;

            if (parentViewHolder.shouldItemViewClickToggleExpansion()) {
                parentViewHolder.setMainItemClickToExpand();
            }

            ParentWrapper parentWrapper = (ParentWrapper) helperItem;
            parentViewHolder.setExpanded(parentWrapper.isExpanded());
            onBindParentViewHolder(parentViewHolder, position, parentWrapper.getParentListItem());
        } else if (helperItem == null) {
            throw new IllegalStateException("Incorrect ViewHolder found");
        } else {
            onBindChildViewHolder((CVH) holder, position, helperItem);
        }
    }

    /**
     * Creates the Parent ViewHolder. Called from onCreateViewHolder when the item is a ParenObject.
     *
     * @param parentViewGroup
     * @return ParentViewHolder that the user must create and inflate.
     */
    public abstract PVH onCreateParentViewHolder(ViewGroup parentViewGroup);

    /**
     * Creates the Child ViewHolder. Called from onCreateViewHolder when the item is a ChildObject.
     *
     * @param childViewGroup
     * @return ChildViewHolder that the user must create and inflate.
     */
    public abstract CVH onCreateChildViewHolder(ViewGroup childViewGroup);

    /**
     * Binds the data to the ParentViewHolder. Called from onBindViewHolder when the item is a
     * ParentObject
     *
     * @param parentViewHolder
     * @param position
     */
    public abstract void onBindParentViewHolder(PVH parentViewHolder, int position, Object parentObject);

    /**
     * Binds the data to the ChildViewHolder. Called from onBindViewHolder when the item is a
     * ChildObject
     *
     * @param childViewHolder
     * @param position
     */
    public abstract void onBindChildViewHolder(CVH childViewHolder, int position, Object childObject);

    /**
     * Returns the size of the list that contains Parent and Child objects
     *
     * @return integer value of the size of the Parent/Child list
     */
    @Override
    public int getItemCount() {
        return mHelperItemList.size();
    }

    /**
     * Returns the type of view that the item at the given position is.
     *
     * @param position
     * @return TYPE_PARENT (0) for ParentObjects and TYPE_CHILD (1) for ChildObjects
     * @throws IllegalStateException if the item at the given position in the list is null
     */
    @Override
    public int getItemViewType(int position) {
        Object helperItem = getHelperItem(position);
        if (helperItem instanceof ParentWrapper) {
            return TYPE_PARENT;
        } else if (helperItem == null) {
            throw new IllegalStateException("Null object added");
        } else {
            return TYPE_CHILD;
        }
    }

    @Override
    public void onParentListItemExpanded(int position) {
        Object helperItem = getHelperItem(position);
        if (helperItem instanceof ParentWrapper) {
            expandHelperItem((ParentWrapper) helperItem, position, false);
        }
    }

    @Override
    public void onParentListItemCollapsed(int position) {
        Object helperItem = getHelperItem(position);
        if (helperItem instanceof ParentWrapper) {
            collapseHelperItem((ParentWrapper) helperItem, position, false);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mAttachedRecyclerViewPool.add(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mAttachedRecyclerViewPool.remove(recyclerView);
    }

    public void addExpandCollapseListener(ExpandableRecyclerAdapter.ExpandCollapseListener expandCollapseListener) {
        mExpandCollapseListener = expandCollapseListener;
    }

    /**
     * Expands the parent with the specified index in the list of parents.
     *
     * @param parentIndex The index of the parent to expand
     */
    public void expandParent(int parentIndex) {
        int parentWrapperIndex = getParentWrapperIndex(parentIndex);

        Object helperItem = getHelperItem(parentWrapperIndex);
        ParentWrapper parentWrapper;
        if (helperItem instanceof ParentWrapper) {
             parentWrapper = (ParentWrapper) helperItem;
        } else {
            return;
        }

        expandViews(parentWrapper, parentWrapperIndex);
    }

    /**
     * Expands the parent associated with a specified {@link ParentListItem} in
     * the list of parents.
     *
     * @param parentObject The {@code ParentObject} of the parent to expand
     */
    public void expandParent(ParentListItem parentObject) {
        ParentWrapper parentWrapper = getParentWrapper(parentObject);
        int parentWrapperIndex = mHelperItemList.indexOf(parentWrapper);
        if (parentWrapperIndex == -1) {
            return;
        }

        expandViews(parentWrapper, parentWrapperIndex);
    }

    /**
     * Expands all parents in the list.
     */
    public void expandAllParents() {
        for (ParentListItem parentObject : mParentItemList) {
            expandParent(parentObject);
        }
    }

    /**
     * Collapses the parent with the specified index in the list of parents.
     *
     * @param parentIndex The index of the parent to expand
     */
    public void collapseParent(int parentIndex) {
        int parentWrapperIndex = getParentWrapperIndex(parentIndex);

        Object helperItem = getHelperItem(parentWrapperIndex);
        ParentWrapper parentWrapper;
        if (helperItem instanceof ParentWrapper) {
            parentWrapper = (ParentWrapper) helperItem;
        } else {
            return;
        }

        collapseViews(parentWrapper, parentWrapperIndex);
    }

    /**
     * Collapses the parent associated with a specified {@link ParentListItem} in
     * the list of parents.
     *
     * @param parentObject The {@code ParentObject} of the parent to collapse
     */
    public void collapseParent(ParentListItem parentObject) {
        ParentWrapper parentWrapper = getParentWrapper(parentObject);
        int parentWrapperIndex = mHelperItemList.indexOf(parentWrapper);
        if (parentWrapperIndex == -1) {
            return;
        }

        collapseViews(parentWrapper, parentWrapperIndex);
    }

    /**
     * Collapses all parents in the list.
     */
    public void collapseAllParents() {
        for (ParentListItem parentObject : mParentItemList) {
            collapseParent(parentObject);
        }
    }

    /**
     * Calls through to the {@link ParentViewHolder} to expand views for each
     * {@link RecyclerView} a specified parent is a child of. These calls to
     * the {@code ParentViewHolder} are made so that animations can be
     * triggered at the {@link android.support.v7.widget.RecyclerView.ViewHolder}
     * level.
     *
     * @param parentIndex The index of the parent to expand
     */
    private void expandViews(ParentWrapper parentWrapper, int parentIndex) {
        for (RecyclerView recyclerView : mAttachedRecyclerViewPool) {
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(parentIndex);
            if (viewHolder != null
                    && !((ParentViewHolder) viewHolder).isExpanded()) {
                ((ParentViewHolder) viewHolder).setExpanded(true);
                ((ParentViewHolder) viewHolder).onExpansionToggled(false);
            }

            expandHelperItem(parentWrapper, parentIndex, true);
        }
    }

    /**
     * Calls through to the {@link ParentViewHolder} to collapse views for each
     * {@link RecyclerView} a specified parent is a child of. These calls to
     * the {@code ParentViewHolder} are made so that animations can be
     * triggered at the {@link android.support.v7.widget.RecyclerView.ViewHolder}
     * level.
     *
     * @param parentIndex The index of the parent to collapse
     */
    private void collapseViews(ParentWrapper parentWrapper, int parentIndex) {
        for (RecyclerView recyclerView : mAttachedRecyclerViewPool) {
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(parentIndex);
            if (viewHolder != null
                    && ((ParentViewHolder) viewHolder).isExpanded()) {
                ((ParentViewHolder) viewHolder).setExpanded(false);
                ((ParentViewHolder) viewHolder).onExpansionToggled(true);
            }

            collapseHelperItem(parentWrapper, parentIndex, true);
        }
    }

    /**
     * Expands a specified parent item. Calls through to the {@link ExpandableRecyclerAdapter.ExpandCollapseListener}
     * and adds children of the specified parent to the total list of items.
     *
     * @param parentWrapper The {@link ParentWrapper} of the parent to expand
     * @param parentIndex The index of the parent to expand
     * @param expansionTriggeredProgrammatically {@value false} if expansion was triggered by a
     *                                                         click event, {@value false} otherwise.
     */
    private void expandHelperItem(ParentWrapper parentWrapper, int parentIndex, boolean expansionTriggeredProgrammatically) {
        if (!parentWrapper.isExpanded()) {
            parentWrapper.setExpanded(true);

            if (!expansionTriggeredProgrammatically
                    && mExpandCollapseListener != null) {
                int expandedCountBeforePosition = getExpandedItemCount(parentIndex);
                mExpandCollapseListener.onListItemCollapsed(parentIndex - expandedCountBeforePosition);
            }

            List<?> childObjectList = parentWrapper.getParentListItem().getChildItemList();
            if (childObjectList != null) {
                int numChildObjects = childObjectList.size();
                for (int i = 0; i < numChildObjects; i++) {
                    mHelperItemList.add(parentIndex + i + 1, childObjectList.get(i));
                    notifyItemInserted(parentIndex + i + 1);
                }
            }
        }
    }

    /**
     * Collapses a specified parent item. Calls through to the {@link ExpandableRecyclerAdapter.ExpandCollapseListener}
     * and adds children of the specified parent to the total list of items.
     *
     * @param parentWrapper The {@link ParentWrapper} of the parent to collapse
     * @param parentIndex The index of the parent to collapse
     * @param collapseTriggeredProgrammatically {@value false} if expansion was triggered by a
     *                                                         click event, {@value false} otherwise.
     */
    private void collapseHelperItem(ParentWrapper parentWrapper, int parentIndex, boolean collapseTriggeredProgrammatically) {
        if (parentWrapper.isExpanded()) {
            parentWrapper.setExpanded(false);

            if (!collapseTriggeredProgrammatically
                    && mExpandCollapseListener != null) {
                int expandedCountBeforePosition = getExpandedItemCount(parentIndex);
                mExpandCollapseListener.onListItemCollapsed(parentIndex - expandedCountBeforePosition);
            }

            List<?> childObjectList = parentWrapper.getParentListItem().getChildItemList();
            if (childObjectList != null) {
                for (int i = childObjectList.size() - 1; i >= 0; i--) {
                    mHelperItemList.remove(parentIndex + i + 1);
                    notifyItemRemoved(parentIndex + i + 1);
                }
            }
        }
    }

    /**
     * Method to get the number of expanded children before the specified position.
     *
     * @param position
     * @return number of expanded children before the specified position
     */
    public int getExpandedItemCount(int position) {
        if (position == 0) {
            return 0;
        }

        int expandedCount = 0;
        for (int i = 0; i < position; i++) {
            Object object = getHelperItem(i);
            if (!(object instanceof ParentWrapper)) {
                expandedCount++;
            }
        }
        return expandedCount;
    }
    public int getExpandedItemCount2(int position) {
        if (position == 0) {
            return 0;
        }

        int expandedCount = 0;
        for (int i = 0; i <= position; i++) {
            Object object = getHelperItem(i);
            if (!(object instanceof ParentWrapper)) {
                expandedCount++;
            }
        }
        return expandedCount;
    }

    /**
     * Generates a HashMap for storing expanded state when activity is rotated or onResume() is called.
     *
     * @param itemList
     * @return HashMap containing the Parents expanded stated stored at the position relative to other parents
     */
    private HashMap<Integer, Boolean> generateExpandedStateMap(List<Object> itemList) {
        HashMap<Integer, Boolean> parentObjectHashMap = new HashMap<>();
        int childCount = 0;
        for (int i = 0; i < itemList.size(); i++) {
            if (itemList.get(i) != null) {
                Object helperItem = getHelperItem(i);
                if (helperItem instanceof ParentWrapper) {
                    ParentWrapper parentWrapper = (ParentWrapper) helperItem;
                    parentObjectHashMap.put(i - childCount, parentWrapper.isExpanded());
                } else {
                    childCount++;
                }
            }
        }
        return parentObjectHashMap;
    }

    /**
     * Should be called from onSaveInstanceState of Activity that holds the RecyclerView. This will
     * make sure to add the generated HashMap as an extra to the bundle to be used in
     * OnRestoreInstanceState().
     *
     * @param savedInstanceStateBundle
     * @return the Bundle passed in with the Id HashMap added if applicable
     */
    public Bundle onSaveInstanceState(Bundle savedInstanceStateBundle) {
        savedInstanceStateBundle.putSerializable(EXPANDED_STATE_MAP, generateExpandedStateMap(mHelperItemList));
        return savedInstanceStateBundle;
    }

    /**
     * Should be called from onRestoreInstanceState of Activity that contains the ExpandingRecyclerView.
     * This will fetch the HashMap that was saved in onSaveInstanceState() and use it to restore
     * the expanded states before the rotation or onSaveInstanceState was called.
     *
     * Assumes list of parent objects is the same as when saveinstancestate was stored
     *
     * @param savedInstanceStateBundle
     */
    public void onRestoreInstanceState(Bundle savedInstanceStateBundle) {
        if (savedInstanceStateBundle == null) {
            return;
        }
        if (!savedInstanceStateBundle.containsKey(EXPANDED_STATE_MAP)) {
            return;
        }
        HashMap<Integer, Boolean> expandedStateMap = (HashMap<Integer, Boolean>) savedInstanceStateBundle.getSerializable(EXPANDED_STATE_MAP);
        int fullCount = 0;
        int childCount = 0;
        while (fullCount < mHelperItemList.size()) {
            Object helperItem = getHelperItem(fullCount);
            if (helperItem instanceof ParentWrapper) {
                ParentWrapper parentWrapper = (ParentWrapper) helperItem;
                if (expandedStateMap.containsKey(fullCount - childCount)) {
                    parentWrapper.setExpanded(expandedStateMap.get(fullCount - childCount));
                    if (parentWrapper.isExpanded() && !parentWrapper.getParentListItem().isInitiallyExpanded()) {
                        List<?> childObjectList = parentWrapper.getParentListItem().getChildItemList();
                        if (childObjectList != null) {
                            for (int j = 0; j < childObjectList.size(); j++) {
                                fullCount++;
                                childCount++;
                                mHelperItemList.add(fullCount, childObjectList.get(j));
                            }
                        }
                    } else if (!parentWrapper.isExpanded() && parentWrapper.getParentListItem().isInitiallyExpanded()) {
                        List<?> childObjectList = parentWrapper.getParentListItem().getChildItemList();
                        for (int j = 0; j < childObjectList.size(); j++) {
                            mHelperItemList.remove(fullCount + 1);
                        }
                    }
                }
            } else {
                childCount++;
            }
            fullCount++;
        }
        notifyDataSetChanged();
    }

    /**
     * Returns the helper item held at the adapter position
     *
     * @param position the index of the helper item to return
     * @return Object at that index, may be a ParentWrapper or child Object
     */
    public Object getHelperItem(int position) {
        return mHelperItemList.get(position);
    }
    /**
     * Returns the helper item held at the adapter position
     *
     * @param parentPosition the index of the parent item to return
     * @return Object at that index, may be a ParentWrapper or child Object
     */
    public Object getParentItem(int parentPosition) {
        return mParentItemList.get(parentPosition);
    }

    /**
     * Gets the index of a {@link ParentWrapper} within the helper item list
     * based on the index of the {@code ParentWrapper}.
     *
     * @param parentIndex The index of the parent in the list of parent items
     * @return The index of the parent in the list of all views in the adapter
     */
    public int getParentWrapperIndex(int parentIndex) {
        int parentCount = 0;
        int numHelperItems = mHelperItemList.size();
        for (int i = 0; i < numHelperItems; i++) {
            if (mHelperItemList.get(i) instanceof ParentWrapper) {
                parentCount++;

                if (parentCount > parentIndex) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Gets the {@link ParentWrapper} for a specified {@link ParentListItem} from
     * the list of parents.
     *
     * @param parentObject A {@code ParentObject} in the list of parents
     * @return If the parent exists on the list, returns its {@code ParentWrapper}.
     *         Otherwise, returns {@value null}.
     */
    private ParentWrapper getParentWrapper(ParentListItem parentObject) {
        int numHelperItems = mHelperItemList.size();
        for (int i = 0; i < numHelperItems; i++) {
            Object helperItem = mHelperItemList.get(i);
            if (helperItem instanceof ParentWrapper) {
                if (((ParentWrapper) helperItem).getParentListItem().equals(parentObject)) {
                    return (ParentWrapper) helperItem;
                }
            }
        }

        return null;
    }
}
