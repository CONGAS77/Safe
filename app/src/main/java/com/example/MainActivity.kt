package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.NotificationCategory
import com.example.domain.model.SessionState
import com.example.domain.model.TabId
import com.example.ui.SafeUiState
import com.example.ui.SafeViewModel
import com.example.ui.components.AlertProtocolModal
import com.example.ui.components.AnomalyModal
import com.example.ui.components.DemoControlDrawer
import com.example.ui.components.SafeCard
import com.example.ui.screens.ActiveSessionScreen
import com.example.ui.screens.ActivityScreen
import com.example.ui.screens.CheckInModal
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.CreateSessionWizard
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.LocalSafePalette
import com.example.ui.theme.SafeTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: SafeViewModel = viewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val notifLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                ) { _ -> }
                LaunchedEffect(state.onboardingDone) {
                    if (state.onboardingDone) {
                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            SafeTheme(darkTheme = state.darkMode) {
                SafeAppShell(state = state, viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SafeAppShell(
    state: SafeUiState,
    viewModel: SafeViewModel,
) {
    val palette = LocalSafePalette.current
    val t: (String) -> String = { key -> viewModel.t(key) }

    BackHandler(enabled = state.activeTab != TabId.HOME && state.onboardingDone && !state.wizardOpen) {
        viewModel.setActiveTab(TabId.HOME)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bgApp),
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = palette.bgApp,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                SafeTopAppBar(
                    subtitle = t("app.subtitle"),
                    darkMode = state.darkMode,
                    unreadCount = state.unreadNotificationsCount,
                    onToggleDarkMode = viewModel::toggleDarkMode,
                    onOpenNotifications = { viewModel.setNotificationsSheetOpen(true) },
                )
            },
            bottomBar = {
                SafeBottomNav(
                    activeTab = state.activeTab,
                    activeSession = state.activeSession,
                    t = t,
                    onSelectTab = viewModel::setActiveTab,
                    onSafeTap = {
                        if (state.activeSession != null) {
                            viewModel.setActiveTab(TabId.SAFE)
                        } else {
                            viewModel.openWizard()
                        }
                    },
                )
            },
            floatingActionButton = {
                if (state.demo.enabled && state.onboardingDone && !state.wizardOpen) {
                    Surface(
                        modifier = Modifier
                            .testTag("demo_fab_button")
                            .clickable { viewModel.setDemoDrawerOpen(true) },
                        shape = RoundedCornerShape(999.dp),
                        color = palette.bgSurface,
                        border = BorderStroke(1.dp, palette.borderSubtle),
                        shadowElevation = 6.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(Icons.Filled.Tune, contentDescription = "Mode Démo", modifier = Modifier.size(16.dp), tint = palette.textPrimary)
                            Text("Démo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        }
                    }
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                ) {
                    when (state.activeTab) {
                        TabId.HOME -> HomeScreen(
                            user = state.user,
                            activeSession = state.activeSession,
                            activeCheckIn = state.activeCheckIn,
                            t = t,
                            onOpenWizard = { act -> viewModel.openWizard(act) },
                            onOpenSessionTab = { viewModel.setActiveTab(TabId.SAFE) },
                            onOpenCheckInModal = viewModel::openCheckInModal,
                            onConfirmCheckInOk = viewModel::confirmCheckInOk,
                        )
                        TabId.SAFE -> ActiveSessionScreen(
                            activeSession = state.activeSession,
                            lastCompletedSummary = state.lastCompletedSummary,
                            demo = state.demo,
                            t = t,
                            onOpenWizard = { viewModel.openWizard() },
                            onPauseOrResume = viewModel::pauseOrResumeSession,
                            onExtendSession = viewModel::extendActiveSession,
                            onUpdateDestination = viewModel::updateActiveDestination,
                            onCompleteSession = viewModel::completeActiveSession,
                            onClearSummaryAndGoHome = {
                                viewModel.clearCompletedSummary()
                                viewModel.setActiveTab(TabId.HOME)
                            },
                            onOpenSosProtocol = { reason -> viewModel.openSosProtocol(reason) },
                        )
                        TabId.CONTACTS -> ContactsScreen(
                            contacts = state.contacts,
                            t = t,
                            onAddContact = viewModel::addContact,
                            onUpdateContact = viewModel::updateContact,
                            onDeleteContact = viewModel::deleteContact,
                            onSetPrimaryContact = viewModel::setPrimaryContact,
                        )
                        TabId.ACTIVITY -> ActivityScreen(
                            history = state.history,
                            privacy = state.privacy,
                            t = t,
                        )
                        TabId.SETTINGS -> SettingsScreen(
                            privacy = state.privacy,
                            demo = state.demo,
                            detectionConfig = state.detectionConfig,
                            activeSession = state.activeSession,
                            language = state.language,
                            darkMode = state.darkMode,
                            country = state.country,
                            user = state.user,
                            t = t,
                            onSetLocationPermission = viewModel::setLocationPermission,
                            onSetRetentionPolicy = viewModel::setRetentionPolicy,
                            onDeleteAllData = viewModel::deleteAllUserData,
                            onSetDemoEnabled = viewModel::setDemoEnabled,
                            onUpdateDetectionConfig = viewModel::updateDetectionConfig,
                            onOpenDemoDrawer = { viewModel.setDemoDrawerOpen(true) },
                            onTriggerDemoAnomaly = viewModel::triggerDemoAnomaly,
                            onSetCountry = viewModel::setCountry,
                            onSetLanguage = viewModel::setLanguage,
                            onToggleDarkMode = viewModel::toggleDarkMode,
                            onReplayOnboarding = viewModel::replayOnboarding,
                            onSignInEmail = { email -> viewModel.signInEmail(email) },
                            onSignInGoogle = viewModel::signInGoogle,
                            onAppleSignInNotice = viewModel::appleSignInNotice,
                            onSignOut = viewModel::signOut,
                        )
                    }
                    Spacer(Modifier.height(84.dp))
                }
            }
        }

        // Live in-app toast banner
        AnimatedVisibility(
            visible = state.activeBanner != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 64.dp, start = 16.dp, end = 16.dp)
                .widthIn(max = 560.dp),
        ) {
            state.activeBanner?.let { banner ->
                val isWarn = banner.category == NotificationCategory.DELAY ||
                    banner.category == NotificationCategory.CHECKIN_DUE ||
                    banner.category == NotificationCategory.CHECKIN_UNCONFIRMED
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = palette.bgSurface,
                    border = BorderStroke(1.5.dp, if (isWarn) palette.orange500 else palette.green500),
                    shadowElevation = 8.dp,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(banner.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = palette.textPrimary)
                            Text(banner.body, fontSize = 12.sp, color = palette.textSecondary)
                        }
                        IconButton(onClick = viewModel::dismissBanner) {
                            Icon(Icons.Filled.Close, contentDescription = "Fermer", modifier = Modifier.size(16.dp), tint = palette.textPrimary)
                        }
                    }
                }
            }
        }

        // Full-screen flows
        if (!state.onboardingDone) {
            OnboardingScreen(
                t = t,
                onComplete = viewModel::completeOnboarding,
            )
        }

        if (state.wizardOpen) {
            CreateSessionWizard(
                presetActivity = state.wizardPresetActivity,
                presetDestinations = state.presetDestinations,
                contacts = state.contacts,
                t = t,
                resolveDestination = viewModel::resolveDestination,
                onAddContact = viewModel::addContact,
                onStartSession = viewModel::startNewSession,
                onClose = viewModel::closeWizard,
            )
        }

        if (state.checkInModalOpen) {
            CheckInModal(
                contacts = state.contacts,
                t = t,
                buildDefaultMessage = viewModel::buildDefaultCheckInMessage,
                onSchedule = viewModel::scheduleCheckIn,
                onClose = viewModel::closeCheckInModal,
            )
        }

        AnomalyModal(
            activeSession = state.activeSession,
            dueCheckIn = state.dueCheckIn,
            t = t,
            onConfirmAnomalyOk = viewModel::confirmAnomalyOk,
            onExtendSession = viewModel::extendActiveSession,
            onOpenSosProtocol = { reason -> viewModel.openSosProtocol(reason) },
            onConfirmCheckInOk = viewModel::confirmCheckInOk,
            onEscalateCheckIn = viewModel::escalateCheckIn,
        )

        AlertProtocolModal(
            alertModalOpen = state.alertModalOpen,
            alertDraft = state.alertDraft,
            alertService = viewModel.alertService,
            country = state.country,
            activeSession = state.activeSession,
            t = t,
            onClose = viewModel::closeAlertModal,
        )

        DemoControlDrawer(
            demo = state.demo,
            activeSession = state.activeSession,
            activeCheckIn = state.activeCheckIn,
            demoDrawerOpen = state.demoDrawerOpen,
            onCloseDrawer = { viewModel.setDemoDrawerOpen(false) },
            onUpdateDemo = viewModel::updateDemo,
            onTriggerDemoAnomaly = viewModel::triggerDemoAnomaly,
            onSimulateArrival = viewModel::simulateArrival,
            onSimulateCheckInDueNow = viewModel::simulateCheckInDueNow,
            onSimulateCheckInExpiredNow = viewModel::simulateCheckInExpiredNow,
            onOpenSosProtocol = { reason -> viewModel.openSosProtocol(reason) },
        )

        if (state.notificationsSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.setNotificationsSheetOpen(false) },
                containerColor = palette.bgSurface,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                ) {
                    Text("Notifications", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = palette.textPrimary)
                    Spacer(Modifier.height(12.dp))
                    if (state.notifications.isEmpty()) {
                        SafeCard {
                            Text("Aucune notification récente.", fontSize = 14.sp, color = palette.textSecondary)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.notifications.take(10).forEach { notif ->
                                val time = SimpleDateFormat("HH:mm", Locale.FRANCE).format(Date(notif.createdAt))
                                SafeCard {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = palette.textPrimary)
                                        Text(time, fontSize = 12.sp, color = palette.textMuted)
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(notif.body, fontSize = 13.sp, color = palette.textSecondary)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun SafeTopAppBar(
    subtitle: String,
    darkMode: Boolean,
    unreadCount: Int,
    onToggleDarkMode: () -> Unit,
    onOpenNotifications: () -> Unit,
) {
    val palette = LocalSafePalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.bgApp)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(
                        Brush.linearGradient(listOf(palette.green500, palette.green600)),
                        RoundedCornerShape(11.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("S", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
            Column {
                Text(
                    text = "SAFE",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.7.sp,
                    color = palette.textPrimary,
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = palette.textMuted,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                modifier = Modifier.size(44.dp).testTag("header_theme_button"),
                shape = RoundedCornerShape(12.dp),
                color = palette.bgSurface,
                border = BorderStroke(1.dp, palette.borderSubtle),
                onClick = onToggleDarkMode,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (darkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = "Changer le thème",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Surface(
                modifier = Modifier.size(44.dp).testTag("header_notifications_button"),
                shape = RoundedCornerShape(12.dp),
                color = palette.bgSurface,
                border = BorderStroke(1.dp, palette.borderSubtle),
                onClick = onOpenNotifications,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(8.dp)
                                .background(palette.green500, CircleShape),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SafeBottomNav(
    activeTab: TabId,
    activeSession: com.example.domain.model.SafeSession?,
    t: (String) -> String,
    onSelectTab: (TabId) -> Unit,
    onSafeTap: () -> Unit,
) {
    val palette = LocalSafePalette.current
    val centerBrush = when (activeSession?.state) {
        SessionState.ESCALATED -> Brush.linearGradient(listOf(palette.red500, palette.red600))
        SessionState.WARNING -> Brush.linearGradient(listOf(palette.orange500, palette.orange600))
        else -> Brush.linearGradient(listOf(palette.green500, palette.green600))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = palette.bgSurface,
    ) {
        Column {
            HorizontalDivider(color = palette.borderSubtle)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BottomNavItem(
                    icon = Icons.Filled.Home,
                    label = t("nav.home"),
                    selected = activeTab == TabId.HOME,
                    testTag = "nav_home",
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectTab(TabId.HOME) },
                )
                BottomNavItem(
                    icon = Icons.Filled.Group,
                    label = t("nav.contacts"),
                    selected = activeTab == TabId.CONTACTS,
                    testTag = "nav_contacts",
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectTab(TabId.CONTACTS) },
                )

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .offset(y = (-6).dp)
                        .defaultMinSize(minHeight = 48.dp)
                        .padding(horizontal = 4.dp)
                        .testTag("nav_safe")
                        .clickable(onClick = onSafeTap),
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 6.dp,
                    color = Color.Transparent,
                ) {
                    Box(
                        modifier = Modifier
                            .background(centerBrush, RoundedCornerShape(16.dp))
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Shield, contentDescription = "SAFE", tint = Color.White, modifier = Modifier.size(22.dp))
                            Text(
                                text = t("nav.safe"),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                            )
                        }
                    }
                }

                BottomNavItem(
                    icon = Icons.Filled.Assignment,
                    label = t("nav.activity"),
                    selected = activeTab == TabId.ACTIVITY,
                    testTag = "nav_activity",
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectTab(TabId.ACTIVITY) },
                )
                BottomNavItem(
                    icon = Icons.Filled.Settings,
                    label = t("nav.settings"),
                    selected = activeTab == TabId.SETTINGS,
                    testTag = "nav_settings",
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectTab(TabId.SETTINGS) },
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalSafePalette.current
    val tint = if (selected) palette.green600 else palette.textMuted
    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag(testTag)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
