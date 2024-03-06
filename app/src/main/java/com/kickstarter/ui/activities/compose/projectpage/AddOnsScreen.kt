package com.kickstarter.ui.activities.compose.projectpage

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography
import com.kickstarter.ui.compose.designsystem.shapes

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun AddOnsScreenPreview() {
    KSTheme {
        Scaffold(
            backgroundColor = colors.backgroundAccentGraySubtle
        ) { padding ->
            AddOnsScreen(
                modifier = Modifier.padding(padding),
                environment = Environment.Builder().build(),
                lazyColumnListState = rememberLazyListState(),
                countryList = listOf(),
                onShippingRuleSelected = {},
                rewardItems = (0..10).map {
                    Reward.builder()
                        .title("Item Number $it")
                        .description("This is a description for item $it")
                        .id(it.toLong())
                        .convertedMinimum((100 * (it + 1)).toDouble())
                        .isAvailable(it != 0)
                        .limit(if (it == 0) 1 else 10)
                        .build()
                },
                project =
                Project.builder()
                    .currency("USD")
                    .currentCurrency("USD")
                    .build(),
                onItemAddedOrRemoved = {},
                onContinueClicked = {}
            )
        }
    }
}

@Composable
fun AddOnsScreen(
    modifier: Modifier,
    environment: Environment,
    lazyColumnListState: LazyListState,
    initialCountryInput: String? = null,
    countryList: List<ShippingRule>,
    onShippingRuleSelected: (ShippingRule) -> Unit,
    rewardItems: List<Reward>,
    project: Project,
    onItemAddedOrRemoved: (Map<Reward, Int>) -> Unit,
    onContinueClicked: () -> Unit
) {

    var countryInput by remember {
        mutableStateOf(initialCountryInput ?: "United States")
    }
    var countryListExpanded by remember {
        mutableStateOf(false)
    }
    val interactionSource = remember {
        MutableInteractionSource()
    }
    var addOnCount by remember {
        mutableStateOf(0)
    }
    val rewardSelections: MutableMap<Reward, Int> = mutableMapOf()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            Column {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(
                        topStart = dimensions.radiusLarge,
                        topEnd = dimensions.radiusLarge
                    ),
                    color = colors.backgroundSurfacePrimary,
                    elevation = dimensions.elevationLarge,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensions.paddingMediumLarge)
                    ) {
                        KSPrimaryGreenButton(
                            onClickAction = onContinueClicked,
                            text =
                            if (addOnCount == 0) stringResource(id = R.string.Skip_add_ons)
                            else {
                                when {
                                    addOnCount == 1 -> environment.ksString()?.format(
                                        stringResource(R.string.Continue_with_quantity_count_add_ons_one),
                                        "quantity_count",
                                        addOnCount.toString()
                                    ) ?: ""

                                    addOnCount > 1 -> environment.ksString()?.format(
                                        stringResource(R.string.Continue_with_quantity_count_add_ons_many),
                                        "quantity_count",
                                        addOnCount.toString()
                                    ) ?: ""

                                    else -> stringResource(id = R.string.Skip_add_ons)
                                }
                            },
                            isEnabled = true
                        )
                    }
                }
            }
        },
        backgroundColor = colors.backgroundAccentGraySubtle
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(
                    start = dimensions.paddingMedium,
                    end = dimensions.paddingMedium,
                    top = dimensions.paddingMedium
                )
                .padding(paddingValues = padding),
            state = lazyColumnListState
        ) {
            item {
                Text(
                    text = stringResource(id = R.string.Customize_your_reward_with_optional_addons),
                    style = typography.title3Bold,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))

                Text(
                    text = stringResource(id = R.string.Your_shipping_location),
                    style = typography.subheadlineMedium,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.height(dimensions.paddingSmall))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { countryListExpanded = false }
                        ),
                ) {
                    TextField(
                        modifier = Modifier
                            .height(dimensions.minButtonHeight)
                            .width(dimensions.countryInputWidth),
                        value = countryInput,
                        onValueChange = {
                            countryInput = it
                            countryListExpanded = true
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        shape = shapes.medium,
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = colors.kds_white,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        textStyle = typography.subheadlineMedium.copy(color = colors.textAccentGreenBold),
                    )

                    AnimatedVisibility(visible = countryListExpanded) {
                        Card(shape = shapes.medium) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color = colors.kds_white)
                            ) {
                                if (countryInput.isNotEmpty()) {
                                    items(
                                        items = countryList.filter {
                                            it.location()?.displayableName()?.lowercase()
                                                ?.contains(countryInput.lowercase()) ?: false
                                        }
                                    ) {
                                        CountryListItems(
                                            item = it,
                                            title = it.location()?.displayableName() ?: "",
                                            onSelect = { country ->
                                                countryInput =
                                                    country.location()?.displayableName() ?: ""
                                                countryListExpanded = false
                                                onShippingRuleSelected(country)
                                            }
                                        )
                                    }
                                } else {
                                    items(countryList) {
                                        CountryListItems(
                                            item = it,
                                            title = it.location()?.displayableName() ?: "",
                                            onSelect = { country ->
                                                countryInput =
                                                    country.location()?.displayableName() ?: ""
                                                countryListExpanded = false
                                                onShippingRuleSelected(country)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            items(
                items = rewardItems
            ) { reward ->
                Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                AddOnsContainer(
                    title = reward.title() ?: "",
                    amount = environment.ksCurrency()?.format(
                        reward.convertedMinimum(),
                        project,
                        true,
                    ) ?: "",
                    shippingAmount = "", // todo in implementation
                    description = reward.description() ?: "",
                    buttonEnabled = reward.isAvailable(),
                    buttonText = stringResource(id = R.string.Add),
                    limit = reward.limit() ?: -1,
                    onItemAddedOrRemoved = { count ->
                        rewardSelections[reward] = count
                        var totalRewardsCount = 0
                        rewardSelections.forEach {
                            totalRewardsCount += it.value
                        }
                        addOnCount = totalRewardsCount
                        onItemAddedOrRemoved(rewardSelections)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(dimensions.paddingDoubleLarge))
            }
        }
    }
}

@Composable
fun CountryListItems(
    item: ShippingRule,
    title: String,
    onSelect: (ShippingRule) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(item) }
            .padding(dimensions.paddingXSmall)
    ) {
        Text(text = title)
    }
}