package com.tfkcolin.cebs_scada.ui



@Composable
fun MyApp(
    modifier: Modifier = Modifier,
    viewModel: AppViewModel = viewModel()
){
    val state = rememberScaffoldState()


    Scaffold(
        modifier = modifier,
        scaffoldState = state,
        floatingActionButton = {
            navState?.let {
                AnimatedVisibility(
                    when(it.destination.route){
                        "edit_key" -> mapKeyTemp.isNotEmpty()
                        "edit_home" -> mapKeyTemp.isNotEmpty()
                        "edit_event" -> pinEventTemp.isNotEmpty()
                        else -> false
                    },
                    enter = slideInVertically(initialOffsetY = { height->height }),
                    exit = slideOutVertically(targetOffsetY = { height->height })
                ){
                    val size by animateDpAsState(
                        targetValue = when (it.destination.route) {
                            "edit_event" -> {
                                if (pinEventTemp.size != 0) 20.dp else 0.dp
                            }
                            else -> {
                                if (mapKeyTemp.size != 0) 20.dp else 0.dp
                            }
                        }
                    )
                    Box{
                        HomeFloatingActionButton(
                            modifier = Modifier
                                .padding(top = 10.dp),
                            onClick = {
                                when(it.destination.route){
                                    "edit_key", "edit_home" -> {
                                        scope.launch {
                                            viewModel.updateMapKeys(mapKeyTemp)
                                            mapKeyTemp.clear()
                                            state.snackbarHostState.showSnackbar("Key command saved")
                                        }
                                    }
                                    "edit_event" -> {
                                        scope.launch {
                                            viewModel.updatePinEvents(pinEventTemp)
                                            pinEventTemp.clear()
                                            state.snackbarHostState.showSnackbar("Event saved")
                                        }
                                    }
                                }
                            },
                            extended = true,
                            icon = {
                                Icon(
                                    modifier = Modifier.padding(end = 5.dp),
                                    imageVector = Icons.Filled.ThumbUp,
                                    contentDescription = when(it.destination.route){
                                        "edit_key", "edit_home" -> { "Save key" }
                                        "edit_event" -> { "Save event" }
                                        else -> null
                                    }
                                )
                            },
                            text = { Text(text = "Save") }
                        )
                        Surface(
                            modifier = Modifier
                                .padding(3.dp)
                                .align(Alignment.TopEnd),
                            color = MaterialTheme.colors.primary.copy(alpha = .7f),
                            shape = CircleShape
                        ) {
                            val text = when(it.destination.route){
                                "edit_event" -> { "${pinEventTemp.size}"}
                                else -> { "${mapKeyTemp.size}"}
                            }
                            Text(
                                modifier = Modifier
                                    .size(size),
                                text = text,
                                color = MaterialTheme.colors.onPrimary,
                                style = MaterialTheme.typography.caption,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "CEBS Scada")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                state.drawerState.open()
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(
                                id = R.drawable.ic_baseline_dehaze_24
                            ),
                            contentDescription = "drawer state"
                        )
                    }
                },
                actions = {
                    navState?.let {
                        if(it.destination.route == "home"
                            && viewModel.mode.value == AppMode.BLUETOOTH){
                            IconButton(
                                onClick = {
                                    if (ActivityCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.BLUETOOTH_SCAN
                                        ) != PackageManager.PERMISSION_GRANTED
                                        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                                    )
                                        requestPermission(Manifest.permission.BLUETOOTH_SCAN)
                                    if(viewModel.scanPermissionGranted.value && viewModel.connectPermissionGranted.value) {
                                        viewModel.startDiscovery(context)
                                        dialogVisible = true
                                    }
                                }
                            ) {
                                Icon(
                                    modifier = Modifier.padding(end = 5.dp),
                                    painter = painterResource(
                                        id = R.drawable.ic_baseline_bluetooth_searching_24
                                    ),
                                    contentDescription = "Search for devices"
                                )
                            }
                        }
                        if(
                            bluetoothState == BluetoothState.STATE_CONNECTED
                            && viewModel.mode.value == AppMode.BLUETOOTH
                        ) {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        state.snackbarHostState.showSnackbar("disconnecting")
                                        withContext(Dispatchers.IO){
                                            viewModel.stop()
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    modifier = Modifier.padding(end = 5.dp),
                                    painter = painterResource(
                                        id = R.drawable.ic_baseline_stop_circle_24
                                    ),
                                    contentDescription = "Search for devices",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                    Box {
                        var expanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            val action: (mode: AppMode) -> Unit = { mode ->
                                viewModel.mode.value = mode
                                keyMapPreferences.setAppMode(mode.ordinal)
                                if(
                                    !viewModel.sendSmsPermissionGranted.value
                                    || !viewModel.receiveSmsPermissionGranted.value
                                    || !viewModel.connectPermissionGranted.value
                                    || !viewModel.scanPermissionGranted.value
                                )
                                    showExplanatoryDialog = true
                                expanded = false
                            }
                            AppMode.values().forEach { mode ->
                                DropdownMenuItem(
                                    onClick = {
                                        action(mode)
                                    }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = viewModel.mode.value == mode,
                                            onCheckedChange = {
                                                action(mode)
                                            }
                                        )
                                        Text(
                                            modifier = Modifier.padding(horizontal = 5.dp),
                                            text = mode.name.lowercase().replaceFirstChar {
                                                it.titlecase(Locale.ROOT)
                                            },
                                            style = MaterialTheme.typography.caption
                                        )
                                    }
                                }
                            }
                            DropdownMenuItem(
                                onClick = {
                                    val lastMode = viewModel.useEncryption.value
                                    keyMapPreferences.setEncryptionMode(!lastMode)
                                    viewModel.useEncryption.value = !lastMode
                                }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = viewModel.useEncryption.value,
                                        onCheckedChange = {
                                            val lastMode = viewModel.useEncryption.value
                                            keyMapPreferences.setEncryptionMode(!lastMode)
                                            viewModel.useEncryption.value = !lastMode
                                        }
                                    )
                                    Text(
                                        modifier = Modifier.padding(horizontal = 5.dp),
                                        text = "Encrypt",
                                        style = MaterialTheme.typography.caption
                                    )
                                }
                            }
                        }
                    }
                }
            )
        },
        drawerContent = {
            LazyColumn {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            modifier = Modifier
                                .size(150.dp)
                                .padding(bottom = 5.dp),
                            painter = painterResource(id = R.drawable.sarlbig),
                            contentDescription = "drawer image"
                        )
                    }
                }
                item{
                    Text(
                        modifier = Modifier
                            .padding(bottom = 10.dp, start = 20.dp, end = 20.dp)
                            .fillMaxWidth(),
                        text = "Committed to brighter people's life",
                        textAlign = TextAlign.Center
                    )
                    Divider(modifier.padding(10.dp))
                }
                item {
                    TextButton(
                        modifier = Modifier
                            .padding(vertical = 5.dp)
                            .fillMaxWidth(),
                        onClick = {
                            if(navState?.destination?.route != "home")
                                navController.navigate("home"){
                                    popUpTo("home"){
                                        inclusive = true
                                    }
                                }
                            if(mapKeyTemp.isNotEmpty())
                                mapKeyTemp.clear()
                            if(pinEventTemp.isNotEmpty())
                                pinEventTemp.clear()
                            scope.launch {
                                state.drawerState.close()
                            }
                        }
                    ) {
                        Icon(
                            modifier = Modifier.padding(end = 15.dp, start = 5.dp),
                            imageVector = Icons.Filled.Home,
                            contentDescription = null
                        )
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Home",
                            textAlign = TextAlign.Start
                        )
                    }
                    TextButton(
                        modifier = Modifier
                            .padding(vertical = 5.dp)
                            .fillMaxWidth(),
                        onClick = {
                            if(navState?.destination?.route != "admin")
                                navController.navigate("admin"){
                                    popUpTo("home")
                                }
                            if(mapKeyTemp.isNotEmpty())
                                mapKeyTemp.clear()
                            if(pinEventTemp.isNotEmpty())
                                pinEventTemp.clear()
                            scope.launch {
                                state.drawerState.close()
                            }
                        }
                    ) {
                        Icon(
                            modifier = Modifier.padding(end = 15.dp, start = 5.dp),
                            imageVector = Icons.Filled.Person,
                            contentDescription = null
                        )
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Administrator",
                            textAlign = TextAlign.Start
                        )
                    }
                }
                item {
                    Divider(modifier.padding(10.dp))
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            navController.navigate("about"){
                                popUpTo("home")
                            }
                            scope.launch {
                                state.drawerState.close()
                            }
                        }
                    ) {
                        Icon(
                            modifier = Modifier.padding(end = 15.dp, start = 5.dp),
                            imageVector = Icons.Filled.Info,
                            contentDescription = "About us"
                        )
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "About us",
                            textAlign = TextAlign.Start
                        )
                    }
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp),
                            text = "Copyright \u00a9 nov. 2021",
                            style = MaterialTheme.typography.caption
                        )
                        Text(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            text = "Version 1.0.3",
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
            }
        }
    ) {
        Box(contentAlignment = Alignment.Center){
            Column {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = when(bluetoothState){
                        BluetoothState.STATE_CONNECTED -> Color.Green
                        BluetoothState.STATE_CONNECTING -> Color.Yellow
                        BluetoothState.STATE_DISCONNECTED -> Color.Red
                        BluetoothState.STATE_NONE -> Color.DarkGray
                        else -> Color.Blue
                    }
                ){}
                NavHost(
                    modifier = Modifier
                        .padding(it),
                    navController = navController,
                    startDestination = "home"){
                    composable(
                        route = "home"
                    ){
                        LaunchedEffect(Unit){
                            isAdmin = false
                        }
                        HomeScreen(
                            dataList = dataList,
                            mapKeys = mapKey.filter { it.selected },
                            onButtonClicked = { data ->
                                when(viewModel.mode.value){
                                    AppMode.GSM -> {
                                        sendSms(
                                            context = context,
                                            number = keyMapPreferences.phoneNumber() ?: "",
                                            msg = if(viewModel.useEncryption.value)
                                                encrypt(
                                                    "AES/CBC/NoPadding",
                                                    data,
                                                    com.example.cebsscada.util.key,
                                                    iv
                                                )
                                            else
                                                data,
                                            onError = {
                                                scope.launch {
                                                    state
                                                        .snackbarHostState
                                                        .showSnackbar(it ?: "An error occurred")
                                                }
                                            },
                                            onRequestPermission = {
                                                requestPermission(Manifest.permission.SEND_SMS)
                                            }
                                        )
                                    }
                                    AppMode.BLUETOOTH -> {
                                        viewModel.write(
                                            if(viewModel.useEncryption.value)
                                                encrypt(
                                                    "AES/CBC/NoPadding",
                                                    data,
                                                    com.example.cebsscada.util.key,
                                                    iv
                                                )
                                            else
                                                data
                                        ){ err ->
                                            scope.launch {
                                                state.snackbarHostState.showSnackbar("Error: $err")
                                            }
                                        }
                                    }
                                    else -> {}
                                }
                            },
                            testText = testText,
                            onTestTextChanged = { text -> testText = text },
                            isHidden = homeHideKeyboardOn,
                            onHideChanged = {
                                homeHideKeyboardOn = !homeHideKeyboardOn
                            },
                            onGetPinEvent = viewModel::getPinEvent,
                            gridCellNumber = KeyMapPreferences(context).keyboardColumn()
                        )
                    }
                    composable(
                        route = "edit_key"
                    ){
                        val listState = rememberLazyListState()
                        EditKeyScreen(
                            keys = mapKey,
                            state = listState,
                            onHiddenButtonClicked = {
                                scope.launch {
                                    viewModel.deleteMapKey(it)
                                    state
                                        .snackbarHostState
                                        .showSnackbar(
                                            "Key deleted",
                                        )
                                }
                            },
                            onDataChanged = { map ->
                                if(map.id in mapKeyTemp.map { id -> id.id }) {
                                    mapKeyTemp.removeAt(
                                        mapKeyTemp.indexOfFirst { r -> r.id == map.id }
                                    )
                                    mapKeyTemp.add(map)
                                }
                                else
                                    mapKeyTemp.add(map)
                            },
                            temps = mapKeyTemp,
                            onClearTemp = {mapKeyTemp.clear()},
                            onAddKey = {
                                scope.launch {
                                    viewModel.insertMapKeys(listOf(it))
                                    listState.animateScrollToItem(mapKey.lastIndex)
                                }
                            }
                        )
                    }
                    composable(
                        route = "edit_event"
                    ){
                        val listState = rememberLazyListState()
                        EditEventScreen(
                            pins = pinEvents,
                            temps = pinEventTemp,
                            onClearTemp = {pinEventTemp.clear()},
                            state = listState,
                            onPinEventChanged = { pin, key, value ->
                                val out = hashMapOf<String, String>()
                                val temp = pinEventTemp.firstOrNull { e -> e.mcPinId == pin.mcPinId }
                                if(temp != null){
                                    out.putAll(temp.eventsMap)
                                    pinEventTemp.remove(temp)
                                    out[key] = value
                                    pinEventTemp.add(PinEvent(pin.mcPinId, out))
                                }
                                else
                                    pinEventTemp.add(pin)
                            },
                            onHiddenButtonClicked = { pin, key ->
                                val out = hashMapOf<String, String>()
                                val temp = pinEventTemp.firstOrNull { e -> e.mcPinId == pin.mcPinId }
                                if(temp != null){
                                    out.putAll(temp.eventsMap)
                                    pinEventTemp.remove(temp)
                                    out.remove(key)
                                    pinEventTemp.add(PinEvent(pin.mcPinId, out))
                                }
                                scope.launch {
                                    viewModel.updatePinEvent(pin)
                                    state
                                        .snackbarHostState
                                        .showSnackbar("Event deleted")
                                }
                            },
                            onAddEventClicked = {
                                scope.launch {
                                    viewModel.updatePinEvent(it)
                                }
                            },
                            onAddPinEvent = { id ->
                                if(id !in pinEvents.map { p -> p.mcPinId }) {
                                    scope.launch {
                                        viewModel.insertPinEvent(PinEvent(mcPinId = id))
                                        listState.animateScrollToItem(pinEvents.lastIndex)
                                    }
                                }
                                else {
                                    scope.launch {
                                        state.snackbarHostState.showSnackbar("This pin already exist")
                                    }
                                }
                            },
                            onDeletePinEvent = {
                                scope.launch {
                                    viewModel.deletePinEvent(it)
                                }
                            }
                        )
                    }
                    composable(
                        route = "admin"
                    ){
                        ConfigScreen(
                            modifier = Modifier.padding(20.dp),
                            onNavigate = {
                                navController.navigate(it)
                            },
                            state = state,
                            isAdmin = isAdmin,
                            onIsAdminChanged = {
                                isAdmin = it
                            }
                        )
                    }
                    composable(
                        route = "edit_home"
                    ){
                        EditHomeScreen(
                            mapKeys = mapKey,
                            temps = mapKeyTemp,
                            onHiddenButtonClicked = {
                                scope.launch {
                                    viewModel.deleteMapKey(it)
                                }
                            },
                            onKeySelectionChanged = {
                                scope.launch {
                                    if(it.id !in mapKeyTemp.map { e -> e.id }) {
                                        mapKeyTemp.add(it)
                                    }
                                    else{
                                        mapKeyTemp.removeAt(
                                            mapKeyTemp.indexOfFirst { e -> e.id == it.id }
                                        )
                                        mapKeyTemp.add(it)
                                    }
                                }
                            },
                            onClearTemp = {
                                mapKeyTemp.clear()
                            }
                        )
                    }
                    composable(
                        route = "about"
                    ){
                        AboutScreen()
                    }
                }
            }
            AnimatedVisibility(visible = showExplanatoryDialog) {
                Surface(
                    modifier = Modifier
                        .clickable { showExplanatoryDialog = false }
                        .fillMaxSize(),
                    color = MaterialTheme.colors.background.copy(alpha = .1f)
                ) {
                    val screen = remember { arrayListOf<@Composable () -> Unit>() }
                    if(!viewModel.connectPermissionGranted.value){
                        screen.add {
                            Column {
                                Icon(
                                    modifier = Modifier
                                        .padding(5.dp)
                                        .fillMaxWidth(),
                                    painter = painterResource(id = R.drawable.ic_baseline_bluetooth_searching_24),
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.padding(bottom = 15.dp))
                                Text(
                                    modifier = Modifier.padding(5.dp),
                                    text = "This feature require the permission to connect to other" +
                                        " bluetooth devices so that you can start exchanging data with them.")
                                Spacer(modifier = Modifier.padding(bottom = 15.dp))
                                TextButton(
                                    onClick = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                                            launcher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                                        showExplanatoryDialog = false
                                    }
                                ) {
                                    Text(text = "Give us permission")
                                }
                                Spacer(modifier = Modifier.padding(bottom = 15.dp))
                            }
                        }
                    }
                    if(!viewModel.scanPermissionGranted.value){
                        screen.add {
                            Column {
                                Icon(
                                    modifier = Modifier
                                        .padding(5.dp)
                                        .fillMaxWidth(),
                                    painter = painterResource(id = R.drawable.ic_baseline_bluetooth_searching_24),
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.padding(bottom = 15.dp))
                                Text(
                                    modifier = Modifier.padding(5.dp),
                                    text = "This feature require the permission to scan your environment" +
                                        " to detect available bluetooth devices with whom you can connect and interact.\n" +
                                        "no data will be collected in background")
                                Spacer(modifier = Modifier.padding(bottom = 15.dp))
                                TextButton(
                                    onClick = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                                            launcher.launch(Manifest.permission.BLUETOOTH_SCAN)
                                        showExplanatoryDialog = false
                                    }
                                ) {
                                    Text(text = "Give us permission")
                                }
                                Spacer(modifier = Modifier.padding(bottom = 15.dp))
                            }
                        }
                    }
                    if(!viewModel.sendSmsPermissionGranted.value){
                        screen.add {
                            Column {
                                Icon(
                                    modifier = Modifier
                                        .padding(5.dp)
                                        .fillMaxWidth(),
                                    painter = painterResource(id = R.drawable.ic_baseline_textsms_24),
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.padding(bottom = 15.dp))
                                Text(
                                    modifier = Modifier.padding(5.dp),
                                    text = "This feature require to Send sms to the other devices" +
                                        " with whom you want to exchange data.")
                                Spacer(modifier = Modifier.padding(bottom = 15.dp))
                                TextButton(
                                    onClick = {
                                        launcher.launch(Manifest.permission.SEND_SMS)
                                        showExplanatoryDialog = false
                                    }
                                ) {
                                    Text(text = "Give us permission")
                                }
                                Spacer(modifier = Modifier.padding(bottom = 15.dp))
                            }
                        }
                    }
                    if(!viewModel.receiveSmsPermissionGranted.value){
                        screen.add {
                            Column {
                                Icon(
                                    modifier = Modifier
                                        .padding(5.dp)
                                        .fillMaxWidth(),
                                    painter = painterResource(id = R.drawable.ic_baseline_textsms_24),
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.padding(bottom = 15.dp))
                                Text(
                                    modifier = Modifier.padding(5.dp),
                                    text = "This feature require the permission to receive an proceed relevent " +
                                            "sms to the other devices" +
                                            " with whom you want to exchange data.")
                                Spacer(modifier = Modifier.padding(bottom = 15.dp))
                                TextButton(
                                    onClick = {
                                        launcher.launch(Manifest.permission.RECEIVE_SMS)
                                        showExplanatoryDialog = false
                                    }
                                ) {
                                    Text(text = "Give us permission")
                                }
                                Spacer(modifier = Modifier.padding(bottom = 15.dp))
                            }
                        }
                    }
                    ExplanatoryPermissionDialog(
                        modifier = Modifier
                            .fillMaxWidth(.8f),
                        explanatoryPages = screen,
                        onIgnore = {
                            showExplanatoryDialog = false
                        }
                    )
                }
            }
            AnimatedVisibility(
                visible = dialogVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                DeviceListDialog(
                    onDismissRequest = { dialogVisible = false },
                    boundedDevices = viewModel.boundedDevices().value,
                    devices = viewModel.devices(),
                    onDeviceItemClicked = {
                        scope.launch {
                            state.snackbarHostState.showSnackbar("Not implemented yet")
                        }
                    },
                    onBoundedItemClicked = { device ->
                        scope.launch {
                            state.snackbarHostState
                                .showSnackbar("connecting to : ${device.address}")
                            keyMapPreferences.setLastDeviceAddress(device.address)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val permission = when {
                                    ActivityCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.BLUETOOTH_SCAN
                                    ) != PackageManager.PERMISSION_GRANTED -> {
                                        Manifest.permission.BLUETOOTH_SCAN
                                    }
                                    ActivityCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.BLUETOOTH_CONNECT
                                    ) != PackageManager.PERMISSION_GRANTED -> {
                                        Manifest.permission.BLUETOOTH_CONNECT
                                    }
                                    else -> {
                                        null
                                    }
                                }
                                requestPermission(permission)
                            }
                            if(viewModel.scanPermissionGranted.value && viewModel.connectPermissionGranted.value){
                                viewModel.connect(
                                    device,
                                    context,
                                    HC06_UUID
                                ){ err ->
                                    scope.launch {
                                        state.snackbarHostState
                                            .showSnackbar(err)
                                    }
                                }
                            }
                            else
                                state.snackbarHostState
                                    .showSnackbar("We don't have permission to perform this action.")
                        }
                        dialogVisible = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CEBSScadaTheme {

    }
}