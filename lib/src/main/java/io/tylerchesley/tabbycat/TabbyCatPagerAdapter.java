/*
 * Copyright (C) 2015 Tyler Chesley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.tylerchesley.tabbycat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.TabLayoutOnPageChangeListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.List;

import io.tylerchesley.preconditioner.Preconditions;

public class TabbyCatPagerAdapter extends FragmentPagerAdapter {

    private final Context context;
    private final List<Page> pages;

    private TabbyCatPagerAdapter(Context context, FragmentManager fm,
                                List<Page> pages) {
        super(fm);
        this.context = Preconditions.notNull(context, "Context may not be null");
        this.pages = Preconditions.notNull(pages, "Pages may not be null");
    }

    @Override
    public Fragment getItem(int position) {
        final Class<? extends Fragment> fragmentClass = pages.get(position).fragmentClass;
        return Fragment.instantiate(context, fragmentClass.getCanonicalName());
    }

    @Override
    public CharSequence getPageTitle(int position) {
        final Page page = pages.get(position);
        return page.tab.getText();
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    public static String getTitle(Context context, Tab tab) {
        if (tab.value() > -1) {
            return context.getString(tab.value());
        } else if (tab.title() > -1) {
            return context.getString(tab.title());
        } else {
            throw new IllegalArgumentException("No title set on fragment");
        }
    }

    public static Builder build(ViewPager pager, TabLayout tabLayout) {
        return new Builder(pager, tabLayout);
    }

    public static final class Builder {

        private final Context context;
        private final FragmentManager fragmentManager;
        private final TabLayout tabLayout;
        private final ViewPager pager;
        private final List<Page> pages;

        public Builder(ViewPager pager, TabLayout tabLayout) {
            this(pager.getContext(),
                    ((FragmentActivity) pager.getContext()).getSupportFragmentManager(), pager,
                    tabLayout);
        }

        public Builder(Context context, FragmentManager manager, ViewPager pager,
                       TabLayout tabLayout) {
            this.context = Preconditions.notNull(context);
            this.fragmentManager = Preconditions.notNull(manager);
            this.pager = Preconditions.notNull(pager);
            this.tabLayout = Preconditions.notNull(tabLayout);
            this.pages = new ArrayList<>();
        }

        public Builder add(Class<? extends Fragment> fragment) {
            final Tab t = fragment.getAnnotation(Tab.class);
            if (t == null) {
                throw new NullPointerException("Fragment class doesn't have a Tab annotation");
            }
            final TabLayout.Tab tab = newTab().setText(getTitle(context, t));
            if (t.icon() > 0) {
                tab.setIcon(t.icon());
            }
            if (t.layout() > 0) {
                tab.setCustomView(t.layout());
            }
            add(fragment, tab);
            return this;
        }

        public Builder add(Class<? extends Fragment> fragmentClass, TabLayout.Tab tab) {
            return add(new Page(fragmentClass, tab));
        }

        public Builder add(Page page) {
            pages.add(page);
            return this;
        }

        public TabLayout.Tab newTab() {
            return tabLayout.newTab();
        }

        public TabbyCatPagerAdapter build() {
            final TabbyCatPagerAdapter adapter =
                    new TabbyCatPagerAdapter(context, fragmentManager, pages);
            tabLayout.removeAllTabs();
            for (Page page : pages) {
                tabLayout.addTab(page.tab);
            }
            pager.addOnPageChangeListener(new TabLayoutOnPageChangeListener(tabLayout));
            pager.setAdapter(adapter);
            return adapter;
        }

    }

    public static final class Page {

        private final Class<? extends Fragment> fragmentClass;
        private final TabLayout.Tab tab;

        public Page(@NonNull Class<? extends Fragment> fragmentClass, @NonNull TabLayout.Tab tab) {
            this.fragmentClass = fragmentClass;
            this.tab = tab;
        }

        public Class<? extends Fragment> getFragmentClass() {
            return fragmentClass;
        }

        public TabLayout.Tab getTab() {
            return tab;
        }

    }

}
