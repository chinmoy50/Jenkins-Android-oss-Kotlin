package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.CategoryFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Category;
import com.kickstarter.ui.viewholders.ThanksCategoryHolderViewModel;

import org.junit.Test;

import rx.observers.TestSubscriber;

public final class ThanksCategoryHolderViewModelTest extends KSRobolectricTestCase {
  private ThanksCategoryHolderViewModel.ViewModel vm;
  private final TestSubscriber<String> categoryName = new TestSubscriber<>();
  private final TestSubscriber<Category> notifyDelegateOfCategoryClick = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new ThanksCategoryHolderViewModel.ViewModel(environment);
    this.vm.outputs.categoryName().subscribe(this.categoryName);
    this.vm.outputs.notifyDelegateOfCategoryClick().subscribe(this.notifyDelegateOfCategoryClick);
  }

  @Test
  public void testCategoryName() {
    final Category category = CategoryFactory.musicCategory();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(category);
    this.categoryName.assertValues(category.name());
  }

  @Test
  public void testCategoryViewClicked() {
    final Category category = CategoryFactory.bluesCategory();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(category);
    this.vm.inputs.categoryViewClicked();
    this.notifyDelegateOfCategoryClick.assertValues(category);
  }
}
