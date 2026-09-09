import type {

  AnnualRevenueBand,

  BuyerType,

  CompanyProfile,

  CompanyProfileInput,

  EmployeeBand,

} from "./company-types";


export type CompanyFormState = {

  legalName:
    string;

  tradingName:
    string;

  industry:
    string;

  description:
    string;

  websiteUrl:
    string;

  countryCode:
    string;

  headquartersCity:
    string;

  employeeBand:
    EmployeeBand;

  annualRevenueBand:
    AnnualRevenueBand;

  preferredMinContractValue:
    string;

  preferredMaxContractValue:
    string;

  preferredCurrency:
    string;

  yearsExperience:
    string;

  minimumNoticeDays:
    string;

  capabilities:
    string;

  operatingRegions:
    string;

  targetCpvs:
    string;

  certifications:
    string;

  referenceProjects:
    string;

  preferredBuyerTypes:
    BuyerType[];

  excludedKeywords:
    string;

};


export const EMPTY_COMPANY_FORM:
CompanyFormState = {

  legalName:
    "",

  tradingName:
    "",

  industry:
    "",

  description:
    "",

  websiteUrl:
    "",

  countryCode:
    "FR",

  headquartersCity:
    "",

  employeeBand:
    "NOT_DISCLOSED",

  annualRevenueBand:
    "NOT_DISCLOSED",

  preferredMinContractValue:
    "",

  preferredMaxContractValue:
    "",

  preferredCurrency:
    "EUR",

  yearsExperience:
    "",

  minimumNoticeDays:
    "14",

  capabilities:
    "",

  operatingRegions:
    "",

  targetCpvs:
    "",

  certifications:
    "",

  referenceProjects:
    "",

  preferredBuyerTypes:
    [],

  excludedKeywords:
    "",

};


function lines(
  value:
    string,
) {

  return value
    .split(
      "\n",
    )
    .map(
      (
        item,
      ) =>
        item.trim(),
    )
    .filter(
      Boolean,
    );

}


function nullable(
  value:
    string,
) {

  const cleaned =
    value.trim();


  return cleaned ===
    ""

    ? null

    : cleaned;

}


function nullableNumber(
  value:
    string,
) {

  const cleaned =
    value.trim();


  return cleaned ===
    ""

    ? null

    : Number(
        cleaned,
      );

}


export function profileToForm(

  profile:
    CompanyProfile,

): CompanyFormState {

  if (
    !profile.exists
  ) {

    return {
      ...EMPTY_COMPANY_FORM,
    };

  }


  return {

    legalName:
      profile.legalName
      ??
      "",

    tradingName:
      profile.tradingName
      ??
      "",

    industry:
      profile.industry
      ??
      "",

    description:
      profile.description
      ??
      "",

    websiteUrl:
      profile.websiteUrl
      ??
      "",

    countryCode:
      profile.countryCode
      ??
      "FR",

    headquartersCity:
      profile.headquartersCity
      ??
      "",

    employeeBand:
      profile.employeeBand
      ??
      "NOT_DISCLOSED",

    annualRevenueBand:
      profile.annualRevenueBand
      ??
      "NOT_DISCLOSED",

    preferredMinContractValue:
      profile.preferredMinContractValue?.toString()
      ??
      "",

    preferredMaxContractValue:
      profile.preferredMaxContractValue?.toString()
      ??
      "",

    preferredCurrency:
      profile.preferredCurrency
      ??
      "EUR",

    yearsExperience:
      profile.yearsExperience?.toString()
      ??
      "",

    minimumNoticeDays:
      profile.minimumNoticeDays?.toString()
      ??
      "",

    capabilities:
      profile.capabilities.join(
        "\n",
      ),

    operatingRegions:
      profile.operatingRegions
        .map(
          (
            region,
          ) =>
            `${region.countryCode}|${region.regionCode}|${region.regionName}`,
        )
        .join(
          "\n",
        ),

    targetCpvs:
      profile.targetCpvs
        .map(
          (
            cpv,
          ) =>
            `${cpv.code}|${cpv.label}`,
        )
        .join(
          "\n",
        ),

    certifications:
      profile.certifications
        .map(
          (
            certification,
          ) =>
            [

              certification.name,

              certification.issuer,

              certification.certificateNumber
              ??
              "",

              certification.validFrom
              ??
              "",

              certification.expiresOn
              ??
              "",

            ].join(
              "|",
            ),
        )
        .join(
          "\n",
        ),

    referenceProjects:
      profile.referenceProjects
        .map(
          (
            project,
          ) =>
            [

              project.projectName,

              project.clientName,

              project.countryCode,

              project.regionName
              ??
              "",

              project.contractValue?.toString()
              ??
              "",

              project.currency
              ??
              "",

              project.startedOn
              ??
              "",

              project.completedOn
              ??
              "",

              project.description,

            ].join(
              "|",
            ),
        )
        .join(
          "\n",
        ),

    preferredBuyerTypes:
      profile.preferredBuyerTypes,

    excludedKeywords:
      profile.excludedKeywords.join(
        "\n",
      ),

  };

}


export function formToInput(

  form:
    CompanyFormState,

): CompanyProfileInput {

  const operatingRegions =

    lines(
      form.operatingRegions,
    )
      .map(
        (
          line,
        ) => {

          const [
            countryCode,
            regionCode,
            regionName,
          ] =
            line
              .split(
                "|",
              )
              .map(
                (
                  value,
                ) =>
                  value.trim(),
              );


          if (
            !countryCode
            ||
            !regionCode
            ||
            !regionName
          ) {

            throw new Error(
              `Invalid region line: ${line}`,
            );

          }


          return {

            countryCode,

            regionCode,

            regionName,

          };

        },
      );


  const targetCpvs =

    lines(
      form.targetCpvs,
    )
      .map(
        (
          line,
        ) => {

          const separator =
            line.indexOf(
              "|",
            );


          if (
            separator <
            1
          ) {

            throw new Error(
              `Invalid CPV line: ${line}`,
            );

          }


          return {

            code:
              line
                .slice(
                  0,
                  separator,
                )
                .trim(),

            label:
              line
                .slice(
                  separator + 1,
                )
                .trim(),

          };

        },
      );


  const certifications =

    lines(
      form.certifications,
    )
      .map(
        (
          line,
        ) => {

          const fields =
            line
              .split(
                "|",
              )
              .map(
                (
                  value,
                ) =>
                  value.trim(),
              );


          if (
            fields.length <
            2
          ) {

            throw new Error(
              `Invalid certification line: ${line}`,
            );

          }


          return {

            name:
              fields[0]
              ??
              "",

            issuer:
              fields[1]
              ??
              "",

            certificateNumber:
              nullable(
                fields[2]
                ??
                "",
              ),

            validFrom:
              nullable(
                fields[3]
                ??
                "",
              ),

            expiresOn:
              nullable(
                fields[4]
                ??
                "",
              ),

          };

        },
      );


  const referenceProjects =

    lines(
      form.referenceProjects,
    )
      .map(
        (
          line,
        ) => {

          const fields =
            line
              .split(
                "|",
              )
              .map(
                (
                  value,
                ) =>
                  value.trim(),
              );


          if (
            fields.length <
            9
          ) {

            throw new Error(
              `Invalid reference-project line: ${line}`,
            );

          }


          return {

            projectName:
              fields[0]
              ??
              "",

            clientName:
              fields[1]
              ??
              "",

            countryCode:
              fields[2]
              ??
              "",

            regionName:
              nullable(
                fields[3]
                ??
                "",
              ),

            contractValue:
              nullableNumber(
                fields[4]
                ??
                "",
              ),

            currency:
              nullable(
                fields[5]
                ??
                "",
              ),

            startedOn:
              nullable(
                fields[6]
                ??
                "",
              ),

            completedOn:
              nullable(
                fields[7]
                ??
                "",
              ),

            description:
              fields
                .slice(
                  8,
                )
                .join(
                  " | ",
                ),

          };

        },
      );


  return {

    legalName:
      form.legalName.trim(),

    tradingName:
      nullable(
        form.tradingName,
      ),

    industry:
      form.industry.trim(),

    description:
      form.description.trim(),

    websiteUrl:
      nullable(
        form.websiteUrl,
      ),

    countryCode:
      form.countryCode
        .trim()
        .toUpperCase(),

    headquartersCity:
      form.headquartersCity.trim(),

    employeeBand:
      form.employeeBand,

    annualRevenueBand:
      form.annualRevenueBand,

    preferredMinContractValue:
      nullableNumber(
        form.preferredMinContractValue,
      ),

    preferredMaxContractValue:
      nullableNumber(
        form.preferredMaxContractValue,
      ),

    preferredCurrency:
      nullable(
        form.preferredCurrency,
      )?.toUpperCase()
      ??
      null,

    yearsExperience:
      nullableNumber(
        form.yearsExperience,
      ),

    minimumNoticeDays:
      nullableNumber(
        form.minimumNoticeDays,
      ),

    capabilities:
      lines(
        form.capabilities,
      ),

    operatingRegions,

    targetCpvs,

    certifications,

    referenceProjects,

    preferredBuyerTypes:
      form.preferredBuyerTypes,

    excludedKeywords:
      lines(
        form.excludedKeywords,
      ),

  };

}
