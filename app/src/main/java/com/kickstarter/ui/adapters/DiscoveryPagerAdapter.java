package com.kickstarter.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.kickstarter.libs.utils.DiscoveryUtils;
import com.kickstarter.models.Category;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.fragments.DiscoveryFragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DiscoveryPagerAdapter extends FragmentPagerAdapter {
  private final Delegate delegate;
  private List<String> pageTitles;
  private static Map<Integer, DiscoveryFragment> fragmentMap;

  public interface Delegate {
    void discoveryPagerAdapterSetPrimaryPage(DiscoveryPagerAdapter adapter, int position);
  }

  public DiscoveryPagerAdapter(final @NonNull FragmentManager fragmentManager, final @NonNull List<String> pageTitles,
    final Delegate delegate) {
    super(fragmentManager);
    this.delegate = delegate;
    this.pageTitles = pageTitles;
    if (fragmentMap == null) {
      fragmentMap = new HashMap<>();
    }
  }

  @Override
  public void setPrimaryItem(final @NonNull ViewGroup container, final int position, final @NonNull Object object) {
    super.setPrimaryItem(container, position, object);
    delegate.discoveryPagerAdapterSetPrimaryPage(this, position);
  }

  @Override
  public @NonNull DiscoveryFragment getItem(final int position) {
    final DiscoveryFragment fragment = DiscoveryFragment.newInstance(position);
    fragmentMap.put(position, fragment);
    return fragment;
  }

  @Override
  public int getCount() {
    return DiscoveryParams.Sort.values().length;
  }

  @Override
  public CharSequence getPageTitle(final int position) {
    return pageTitles.get(position);
  }

  /**
   * Passes along root categories to its fragment position to help fetch appropriate projects.
   */
  public void takeCategoriesForPosition(final @NonNull List<Category> categories, final int position) {
    safeGetFragment(position).takeCategories(categories);
  }

  /**
   * Take current params from activity and pass to the appropriate fragment.
   */
  public void takeParams(final @NonNull DiscoveryParams params) {
    final int position = DiscoveryUtils.positionFromSort(params.sort());
    safeGetFragment(position).updateParams(params);
  }

  /**
   * Call when the view model tells us to clear specific pages.
   */
  public void clearPages(final @NonNull List<Integer> pages) {
    for (int page : pages) {
      safeGetFragment(page).clearPage();
    }
  }

  /**
   * Don't pull directly from the map as it may be null
   */
  private @NonNull DiscoveryFragment safeGetFragment(final int position) {
    DiscoveryFragment fragment = null;

    if (fragmentMap == null) {
      fragmentMap = new HashMap<>();
    }

    if (fragmentMap.containsKey(position)) {
      fragment = fragmentMap.get(position);
    }

    if (fragment == null) {
      fragment = getItem(position);
    }

    return fragment;
  }
}
