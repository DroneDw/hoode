data class AdminDialogState(
    val showServiceDialog: Boolean = false,
    val showMarketDialog: Boolean = false,
    val showFoodDialog: Boolean = false,
    val showPropertyDialog: Boolean = false,
    val showEventDialog: Boolean = false,
    val showAnnouncementDialog: Boolean = false,
    val showCompanyMarketDialog: Boolean = false, // Add this line
    val selectedProviderId: String = "",
    val selectedSellerId: String = "",
    val selectedOrganizerId: String = "",
    val selectedOrganizerName: String = ""
)
