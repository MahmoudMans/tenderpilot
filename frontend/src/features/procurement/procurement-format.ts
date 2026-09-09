export function formatEnum(
  value:
    string,
) {

  return value
    .replaceAll(
      "_",
      " ",
    )
    .toLowerCase()
    .replace(
      /\b\w/g,
      (
        character,
      ) =>
        character.toUpperCase(),
    );

}


export function formatMoney(

  value:
    number
    |
    null,

  currency:
    string
    |
    null,

) {

  if (
    value ===
    null
  ) {

    return "—";

  }


  return new Intl.NumberFormat(

    "en",

    {

      style:
        "currency",

      currency:
        currency
        ??
        "EUR",

      maximumFractionDigits:
        0,

    },

  ).format(
    value,
  );

}


export function formatValueRange(

  minimum:
    number
    |
    null,

  maximum:
    number
    |
    null,

  currency:
    string
    |
    null,

) {

  if (
    minimum ===
    null
    &&
    maximum ===
    null
  ) {

    return "Value not disclosed";

  }


  if (
    minimum !==
    null
    &&
    maximum !==
    null
  ) {

    return `${formatMoney(minimum, currency)} – ${formatMoney(maximum, currency)}`;

  }


  if (
    minimum !==
    null
  ) {

    return `From ${formatMoney(minimum, currency)}`;

  }


  return `Up to ${formatMoney(maximum, currency)}`;

}


export function formatDate(
  value:
    string
    |
    null,
) {

  if (
    value ===
    null
  ) {

    return "Not specified";

  }


  return new Intl.DateTimeFormat(

    "en",

    {

      dateStyle:
        "medium",

    },

  ).format(
    new Date(
      value,
    ),
  );

}


export function formatDateTime(
  value:
    string
    |
    null,
) {

  if (
    value ===
    null
  ) {

    return "Not specified";

  }


  return new Intl.DateTimeFormat(

    "en",

    {

      dateStyle:
        "medium",

      timeStyle:
        "short",

    },

  ).format(
    new Date(
      value,
    ),
  );

}
