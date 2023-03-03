package xyz.cofe.files.jnr

import FileStat.ModeFlags
import FileStat.ModeFlag

case class FilePerm(
  stiky: Boolean,
  suid: Boolean,
  sgid: Boolean,
  readOwner: Boolean,
  writeOwner: Boolean,
  executeOwner: Boolean,
  readGroup: Boolean,
  writeGroup: Boolean,
  executeGroup: Boolean,
  readOthers: Boolean,
  writeOthers: Boolean,
  executeOthers: Boolean,
):
  def modeFlags:ModeFlags = 
    ModeFlags(
        (if stiky then ModeFlag.Stiky.mask else 0)
      | (if suid  then ModeFlag.Suid.mask else 0)
      | (if sgid  then ModeFlag.Sgid.mask else 0)

      | (if readOwner    then ModeFlag.ReadOwner.mask    else 0)
      | (if writeOwner   then ModeFlag.WriteOwner.mask   else 0)
      | (if executeOwner then ModeFlag.ExecuteOwner.mask else 0)

      | (if readGroup    then ModeFlag.ReadGroup.mask else 0)
      | (if writeGroup   then ModeFlag.WriteGroup.mask else 0)
      | (if executeGroup then ModeFlag.ExecuteGroup.mask else 0)

      | (if readOthers    then ModeFlag.ReadOthers.mask else 0)
      | (if writeOthers   then ModeFlag.WriteOthers.mask else 0)
      | (if executeOthers then ModeFlag.ExecuteOthers.mask else 0)
    )
