package com.example.config

import com.example.domain.model.CountryCode
import com.example.domain.model.EmergencyConfig

object EmergencyNumbers {
    val byCountry: Map<CountryCode, EmergencyConfig> = mapOf(
        CountryCode.FR to EmergencyConfig(
            country = CountryCode.FR,
            countryName = "France",
            primaryNumber = "112",
            policeNumber = "17",
            medicalNumber = "15",
            fireNumber = "18",
            disclaimerKey = "emergency.disclaimer.fr",
        ),
        CountryCode.BE to EmergencyConfig(
            country = CountryCode.BE,
            countryName = "Belgique",
            primaryNumber = "112",
            policeNumber = "101",
            medicalNumber = "112",
            fireNumber = "112",
            disclaimerKey = "emergency.disclaimer.eu",
        ),
        CountryCode.CH to EmergencyConfig(
            country = CountryCode.CH,
            countryName = "Suisse",
            primaryNumber = "112",
            policeNumber = "117",
            medicalNumber = "144",
            fireNumber = "118",
            disclaimerKey = "emergency.disclaimer.ch",
        ),
        CountryCode.CA to EmergencyConfig(
            country = CountryCode.CA,
            countryName = "Canada",
            primaryNumber = "911",
            policeNumber = "911",
            medicalNumber = "911",
            fireNumber = "911",
            disclaimerKey = "emergency.disclaimer.na",
        ),
        CountryCode.GB to EmergencyConfig(
            country = CountryCode.GB,
            countryName = "United Kingdom",
            primaryNumber = "999",
            policeNumber = "999",
            medicalNumber = "999",
            fireNumber = "999",
            disclaimerKey = "emergency.disclaimer.gb",
        ),
        CountryCode.US to EmergencyConfig(
            country = CountryCode.US,
            countryName = "United States",
            primaryNumber = "911",
            policeNumber = "911",
            medicalNumber = "911",
            fireNumber = "911",
            disclaimerKey = "emergency.disclaimer.na",
        ),
        CountryCode.EU to EmergencyConfig(
            country = CountryCode.EU,
            countryName = "Union Européenne",
            primaryNumber = "112",
            policeNumber = "112",
            medicalNumber = "112",
            fireNumber = "112",
            disclaimerKey = "emergency.disclaimer.eu",
        ),
    )

    fun getEmergencyConfig(country: CountryCode): EmergencyConfig =
        byCountry[country] ?: byCountry.getValue(CountryCode.FR)
}
