package org.broadinstitute.dsde.workbench.leonardo.config

final case class ImageConfig(
  welderDockerImage: String,
  jupyterImage: String,
  legacyJupyterImage: String,
  jupyterImageRegex: String,
  rstudioImageRegex: String,
  broadDockerhubImageRegex: String
)
